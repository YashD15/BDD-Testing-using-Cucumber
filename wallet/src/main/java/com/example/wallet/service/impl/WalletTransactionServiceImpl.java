
package com.example.wallet.service.impl;

import com.example.wallet.dto.CreateTransactionRequestDto;
import com.example.wallet.dto.WalletTransactionResponseDto;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.entity.Wallet;
import com.example.wallet.entity.WalletTransaction;
import com.example.wallet.exception.BusinessConflictException;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.repository.WalletTransactionRepository;
import com.example.wallet.service.WalletTransactionService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletTransactionServiceImpl
    implements WalletTransactionService {

  private final WalletRepository walletRepository;
  private final WalletTransactionRepository transactionRepository;

  public WalletTransactionServiceImpl(
      WalletRepository walletRepository,
      WalletTransactionRepository transactionRepository) {
    this.walletRepository = walletRepository;
    this.transactionRepository = transactionRepository;
  }

  @Override
  @Transactional
  public WalletTransactionResponseDto createTransaction(
      CreateTransactionRequestDto request) {

    if (request.getAmount() == null
        || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException(
          "Transaction amount must be greater than zero");
    }

    if (request.getTransactionType() == null) {
      throw new IllegalArgumentException(
          "Transaction type is required");
    }

    Wallet wallet = walletRepository
        .findActiveWalletByUserIdForUpdate(request.getUserId())
        .orElseThrow(() -> new WalletNotFoundException(
            "Wallet not found for user ID: "
                + request.getUserId()));

    BigDecimal currentBalance = wallet.getBalance();
    BigDecimal amount = request.getAmount();
    BigDecimal newBalance;

    if (request.getTransactionType() == TransactionType.CREDIT) {
      newBalance = currentBalance.add(amount);
    } else if (request.getTransactionType() == TransactionType.DEBIT) {
      if (currentBalance.compareTo(amount) < 0) {
        throw new BusinessConflictException("Insufficient wallet balance");
      }

      newBalance = currentBalance.subtract(amount);
    } else {
      throw new IllegalArgumentException(
          "Unsupported transaction type");
    }

    wallet.setBalance(newBalance);

    WalletTransaction transaction = WalletTransaction.builder()
        .walletId(wallet.getId())
        .transactionType(request.getTransactionType())
        .amount(amount)
        .balanceAfter(newBalance)
        .description(request.getDescription())
        .build();

    WalletTransaction savedTransaction = transactionRepository.save(transaction);

    return WalletTransactionResponseDto.fromEntity(savedTransaction);
  }

  @Transactional(readOnly = true)
  public Page<WalletTransactionResponseDto> getTransactionsByUserId(
      Long userId,
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

    Page<WalletTransaction> transactions = transactionRepository
        .findByWalletIdOrderByCreatedAtDescIdDesc(
            wallet.getId(),
            pageable);

    return transactions.map(transaction -> WalletTransactionResponseDto.builder()
        .id(transaction.getId())
        .walletId(transaction.getWalletId())
        .transactionType(transaction.getTransactionType())
        .amount(transaction.getAmount())
        .balanceAfter(transaction.getBalanceAfter())
        .description(transaction.getDescription())
        .createdAt(transaction.getCreatedAt())
        .build());
  }
}
