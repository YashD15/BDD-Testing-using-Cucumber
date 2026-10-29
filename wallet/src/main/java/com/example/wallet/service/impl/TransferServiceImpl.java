package com.example.wallet.service.impl;

import com.example.wallet.dto.TransferHistoryFilterDto;
import com.example.wallet.dto.TransferHistoryResponseDto;
import com.example.wallet.dto.TransferRequestDto;
import com.example.wallet.dto.TransferResponseDto;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.entity.Wallet;
import com.example.wallet.entity.WalletTransaction;
import com.example.wallet.entity.WalletTransfer;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.repository.WalletTransactionRepository;
import com.example.wallet.repository.WalletTransferRepository;
import com.example.wallet.service.TransferService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

  private final WalletRepository walletRepository;
  private final WalletTransactionRepository transactionRepository;
  private final WalletTransferRepository transferRepository;

  @Override
  @Transactional
  public TransferResponseDto transferMoney(TransferRequestDto request) {

    if (request == null
        || request.getSenderUserId() == null
        || request.getReceiverUserId() == null
        || request.getAmount() == null) {
      throw new IllegalArgumentException(
          "Sender, receiver, and amount are required");
    }

    Long senderUserId = request.getSenderUserId();
    Long receiverUserId = request.getReceiverUserId();
    BigDecimal amount = request.getAmount();

    if (senderUserId.equals(receiverUserId)) {
      throw new IllegalArgumentException(
          "Sender and receiver cannot be the same user");
    }

    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException(
          "Transfer amount must be greater than zero");
    }

    // Lock both wallets in a consistent order to reduce deadlock risk.
    Wallet senderWallet;
    Wallet receiverWallet;

    if (senderUserId < receiverUserId) {
      senderWallet = getWalletForUpdate(senderUserId);
      receiverWallet = getWalletForUpdate(receiverUserId);
    } else {
      receiverWallet = getWalletForUpdate(receiverUserId);
      senderWallet = getWalletForUpdate(senderUserId);
    }

    if (!senderWallet.getCurrency().equals(receiverWallet.getCurrency())) {
      throw new IllegalArgumentException(
          "Sender and receiver wallets must use the same currency");
    }

    if (senderWallet.getBalance().compareTo(amount) < 0) {
      throw new IllegalArgumentException(
          "Insufficient wallet balance");
    }

    LocalDateTime now = LocalDateTime.now();
    String currency = senderWallet.getCurrency();

    BigDecimal senderBalance = senderWallet.getBalance().subtract(amount);

    BigDecimal receiverBalance = receiverWallet.getBalance().add(amount);

    // Update wallet balances. These are managed entities under the
    // current transaction and will be persisted by JPA.
    senderWallet.setBalance(senderBalance);
    senderWallet.setUpdatedAt(now);

    receiverWallet.setBalance(receiverBalance);
    receiverWallet.setUpdatedAt(now);

    // 1. Save the dedicated transfer record first to obtain its ID.
    WalletTransfer transfer = WalletTransfer.builder()
        .senderWalletId(senderWallet.getId())
        .receiverWalletId(receiverWallet.getId())
        .amount(amount)
        .currency(currency)
        .status("COMPLETED")
        .description(request.getDescription())
        .createdAt(now)
        .build();

    transfer = transferRepository.saveAndFlush(transfer);

    // 2. Record the debit from the sender.
    WalletTransaction debitTransaction = new WalletTransaction();
    debitTransaction.setWalletId(senderWallet.getId());
    debitTransaction.setTransactionType(TransactionType.DEBIT);
    debitTransaction.setAmount(amount);
    debitTransaction.setBalanceAfter(senderBalance);
    debitTransaction.setDescription(
        "Transfer #" + transfer.getId()
            + " to user " + receiverUserId
            + formatDescription(request.getDescription()));
    debitTransaction.setCreatedAt(now);

    transactionRepository.save(debitTransaction);

    // 3. Record the credit to the receiver.
    WalletTransaction creditTransaction = new WalletTransaction();
    creditTransaction.setWalletId(receiverWallet.getId());
    creditTransaction.setTransactionType(TransactionType.CREDIT);
    creditTransaction.setAmount(amount);
    creditTransaction.setBalanceAfter(receiverBalance);
    creditTransaction.setDescription(
        "Transfer #" + transfer.getId()
            + " from user " + senderUserId
            + formatDescription(request.getDescription()));
    creditTransaction.setCreatedAt(now);

    transactionRepository.save(creditTransaction);

    return TransferResponseDto.builder()
        .transferId(transfer.getId())
        .senderUserId(senderUserId)
        .receiverUserId(receiverUserId)
        .amount(amount)
        .currency(currency)
        .senderBalanceAfter(senderBalance)
        .receiverBalanceAfter(receiverBalance)
        .createdAt(now)
        .status("COMPLETED")
        .build();
  }

  private Wallet getWalletForUpdate(Long userId) {
    return walletRepository
        .findActiveWalletByUserIdForUpdate(userId)
        .orElseThrow(() -> new WalletNotFoundException(
            "Wallet not found for user ID: " + userId));
  }

  private String formatDescription(String description) {
    if (description == null || description.isBlank()) {
      return "";
    }

    return ": " + description;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TransferHistoryResponseDto> getTransferHistory(
      Long userId,
      TransferHistoryFilterDto filters,
      int page,
      int size) {

    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException(
          "User ID must be a positive number");
    }

    if (page < 0) {
      throw new IllegalArgumentException(
          "Page number cannot be negative");
    }

    if (size < 1 || size > 100) {
      throw new IllegalArgumentException(
          "Page size must be between 1 and 100");
    }

    TransferHistoryFilterDto effectiveFilters = filters == null
        ? new TransferHistoryFilterDto()
        : filters;

    String status = normalizeStatus(effectiveFilters.getStatus());

    LocalDate startDate = effectiveFilters.getStartDate();
    LocalDate endDate = effectiveFilters.getEndDate();

    if (startDate != null && endDate != null
        && startDate.isAfter(endDate)) {
      throw new IllegalArgumentException(
          "Start date cannot be after end date");
    }

    LocalDateTime startDateTime = startDate == null
        ? null
        : startDate.atStartOfDay();

    LocalDateTime endDateTime = endDate == null
        ? null
        : endDate.plusDays(1).atStartOfDay();

    Wallet wallet = walletRepository
        .findByUserIdAndIsDeletedFalse(userId)
        .orElseThrow(() -> new WalletNotFoundException(
            "Wallet not found for user ID: " + userId));

    Pageable pageable = PageRequest.of(
        page,
        size,
        Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")));

    return transferRepository.findTransferHistoryByWalletId(
        wallet.getId(),
        status,
        startDateTime,
        endDateTime,
        pageable)
        .map(transfer -> TransferHistoryResponseDto.builder()
            .transferId(transfer.getId())
            .senderUserId(
                getUserIdForWallet(transfer.getSenderWalletId()))
            .receiverUserId(
                getUserIdForWallet(transfer.getReceiverWalletId()))
            .amount(transfer.getAmount())
            .currency(transfer.getCurrency())
            .status(transfer.getStatus())
            .description(transfer.getDescription())
            .createdAt(transfer.getCreatedAt())
            .build());
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }

    String normalized = status.trim().toUpperCase(Locale.ROOT);

    try {
      com.example.wallet.entity.TransferStatus.valueOf(normalized);
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException(
          "Unsupported transfer status: " + status);
    }

    return normalized;
  }

  private Long getUserIdForWallet(Long walletId) {
    return walletRepository.findById(walletId)
        .filter(wallet -> !wallet.isDeleted())
        .map(Wallet::getUserId)
        .orElseThrow(() -> new WalletNotFoundException(
            "Wallet not found for wallet ID: " + walletId));
  }
}
