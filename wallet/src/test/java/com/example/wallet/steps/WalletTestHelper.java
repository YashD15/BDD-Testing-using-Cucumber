
package com.example.wallet.steps;

import com.example.wallet.entity.Wallet;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.repository.WalletTransactionRepository;

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
        .ifPresent(wallet -> transactionRepository.deleteAll(
            transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(
                    wallet.getId())));
  }

  @Transactional
  public void deleteWallet(Long userId) {
    walletRepository.findByUserIdAndIsDeletedFalse(userId)
        .ifPresent(wallet -> walletRepository.delete(wallet));
  }

}
