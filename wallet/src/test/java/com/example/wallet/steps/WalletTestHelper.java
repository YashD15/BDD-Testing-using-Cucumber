
package com.example.wallet.steps;

import com.example.wallet.entity.Wallet;
import com.example.wallet.entity.WalletTransaction;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.repository.WalletTransactionRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class WalletTestHelper {

  private final WalletRepository walletRepository;
  private final WalletTransactionRepository transactionRepository;

  public WalletTestHelper(
      WalletRepository walletRepository,
      WalletTransactionRepository transactionRepository) {
    this.walletRepository = walletRepository;
    this.transactionRepository = transactionRepository;
  }

  @Transactional
  public Long createWalletWithBalance(BigDecimal balance) {
    Wallet wallet = Wallet.builder()
        .userId(900001L)
        .balance(balance)
        .currency("INR")
        .isDeleted(false)
        .build();

    return walletRepository.save(wallet).getId();
  }

  @Transactional
  public BigDecimal getWalletBalance(Long userId) {
    return walletRepository.findByUserIdAndIsDeletedFalse(userId)
        .orElseThrow(() -> new IllegalStateException("Test wallet not found"))
        .getBalance();
  }

  @Transactional
  public void resetWallet(Long userId, BigDecimal balance) {
    walletRepository.findByUserIdAndIsDeletedFalse(userId)
        .ifPresent(wallet -> {
          wallet.setBalance(balance);
          walletRepository.save(wallet);
        });
  }

  @Transactional(readOnly = true)
  public boolean walletExists(Long userId) {
    return walletRepository.findByUserIdAndIsDeletedFalse(userId)
        .isPresent();
  }

  @Transactional
  public void clearTransactions(Long userId) {
    walletRepository.findByUserIdAndIsDeletedFalse(userId)
        .ifPresent(wallet -> transactionRepository.deleteByWalletId(wallet.getId()));
  }

  @Transactional
  public void deleteWallet(Long userId) {
    walletRepository.findByUserIdAndIsDeletedFalse(userId)
        .ifPresent(wallet -> walletRepository.delete(wallet));
  }

  public void prepareTransferWallet(Long userId, BigDecimal balance) {
    Wallet wallet = walletRepository
        .findByUserIdAndIsDeletedFalse(userId)
        .orElse(null);

    if (wallet == null) {
      wallet = Wallet.builder()
          .userId(userId)
          .balance(balance)
          .currency("INR")
          .build();
    } else {
      clearTransactions(userId);
      wallet.setBalance(balance);
      wallet.setCurrency("INR");
      wallet.setUpdatedAt(java.time.LocalDateTime.now());
    }

    walletRepository.save(wallet);
  }

  @Transactional
  public void removeTransferWallet(Long userId) {
    walletRepository.findByUserIdAndIsDeletedFalse(userId)
        .ifPresent(wallet -> {
          transactionRepository.deleteByWalletId(wallet.getId());
          walletRepository.delete(wallet);
        });
  }

  public BigDecimal getTransferWalletBalance(Long userId) {
    return walletRepository.findByUserIdAndIsDeletedFalse(userId)
        .orElseThrow(() -> new AssertionError("Wallet not found for user: " + userId))
        .getBalance();
  }

  public java.util.List<WalletTransaction> getTransferTransactions(
      Long userId) {

    Wallet wallet = walletRepository
        .findByUserIdAndIsDeletedFalse(userId)
        .orElse(null);

    if (wallet == null) {
      return java.util.Collections.emptyList();
    }

    return transactionRepository
        .findByWalletIdOrderByCreatedAtDescIdDesc(
            wallet.getId(),
            PageRequest.of(
                0,
                100,
                Sort.by(
                    Sort.Order.desc("createdAt"),
                    Sort.Order.desc("id"))))
        .getContent();
  }

  @Transactional(readOnly = true)
  public boolean walletRecordExists(Long userId) {
    return walletRepository.findByUserId(userId).isPresent();
  }

  @Transactional(readOnly = true)
  public boolean walletIsSoftDeleted(Long userId) {
    return walletRepository.findByUserId(userId)
        .map(Wallet::isDeleted)
        .orElse(false);
  }

  @Transactional(readOnly = true)
  public BigDecimal getWalletBalanceIncludingDeleted(Long userId) {
    return walletRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalStateException("Test wallet record not found"))
        .getBalance();
  }

  @Transactional
  public void removeWalletAndTransactions(Long userId) {
    walletRepository.findByUserId(userId)
        .ifPresent(wallet -> {
          transactionRepository.deleteByWalletId(wallet.getId());
          walletRepository.delete(wallet);
        });
  }

  @Transactional(readOnly = true)
  public java.util.List<WalletTransaction> getTransactionsIncludingDeleted(
      Long userId) {

    Wallet wallet = walletRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalStateException("Test wallet record not found"));

    return transactionRepository
        .findByWalletIdOrderByCreatedAtDescIdDesc(
            wallet.getId(),
            PageRequest.of(0, 100))
        .getContent();
  }

}
