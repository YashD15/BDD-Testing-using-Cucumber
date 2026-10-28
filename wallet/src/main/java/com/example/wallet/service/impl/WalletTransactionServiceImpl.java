
package com.example.wallet.service.impl;

import com.example.wallet.dto.CreateTransactionRequestDto;
import com.example.wallet.dto.WalletTransactionResponseDto;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.entity.Wallet;
import com.example.wallet.entity.WalletTransaction;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.repository.WalletTransactionRepository;
import com.example.wallet.service.WalletTransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
        throw new IllegalArgumentException(
            "Insufficient wallet balance");
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

  @Override
  @Transactional(readOnly = true)
  public List<WalletTransactionResponseDto> getTransactionsByUserId(
      Long userId) {

    Wallet wallet = walletRepository
        .findByUserIdAndIsDeletedFalse(userId)
        .orElseThrow(() -> new WalletNotFoundException(
            "Wallet not found for user ID: " + userId));

    return transactionRepository
        .findByWalletIdOrderByCreatedAtDesc(wallet.getId())
        .stream()
        .map(WalletTransactionResponseDto::fromEntity)
        .toList();
  }
}
