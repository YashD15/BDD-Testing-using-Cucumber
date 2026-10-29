package com.example.wallet.service.impl;

import com.example.wallet.dto.TransferRequestDto;
import com.example.wallet.entity.Wallet;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.repository.WalletTransactionRepository;
import com.example.wallet.repository.WalletTransferRepository;
import com.example.wallet.service.TransferService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransferRollbackIntegrationTest {

  private static final Long SENDER_USER_ID = Long.MAX_VALUE;
  private static final Long RECEIVER_USER_ID = 900022L;

  @Autowired
  private TransferService transferService;

  @Autowired
  private WalletRepository walletRepository;

  @Autowired
  private WalletTransactionRepository transactionRepository;

  @Autowired
  private WalletTransferRepository transferRepository;

  @BeforeEach
  void setUp() {
    clearDatabase();

    LocalDateTime now = LocalDateTime.now();

    Wallet sender = Wallet.builder()
        .userId(SENDER_USER_ID)
        .balance(new BigDecimal("1000.00"))
        .currency("INR")
        .createdAt(now)
        .updatedAt(now)
        .build();

    Wallet receiver = Wallet.builder()
        .userId(RECEIVER_USER_ID)
        .balance(new BigDecimal("300.00"))
        .currency("INR")
        .createdAt(now)
        .updatedAt(now)
        .build();

    walletRepository.saveAndFlush(sender);
    walletRepository.saveAndFlush(receiver);
  }

  @AfterEach
  void tearDown() {
    clearDatabase();
  }

  @Test
  void transferMoney_shouldRollbackEverythingWhenCreditTransactionFails() {

    // The debit description should fit the database column,
    // but the credit description should exceed its 255-character limit.
    String longDescription = "x".repeat(220);

    TransferRequestDto request = TransferRequestDto.builder()
        .senderUserId(SENDER_USER_ID)
        .receiverUserId(RECEIVER_USER_ID)
        .amount(new BigDecimal("200.00"))
        .description(longDescription)
        .build();

    // The service must be called through the Spring-managed bean
    // so its @Transactional boundary is applied.
    assertThrows(
        RuntimeException.class,
        () -> transferService.transferMoney(request));

    // Reload the wallets from the database.
    Wallet senderAfter = walletRepository
        .findByUserIdAndIsDeletedFalse(SENDER_USER_ID)
        .orElseThrow();

    Wallet receiverAfter = walletRepository
        .findByUserIdAndIsDeletedFalse(RECEIVER_USER_ID)
        .orElseThrow();

    // Neither wallet balance should have changed.
    assertEquals(
        0,
        new BigDecimal("1000.00")
            .compareTo(senderAfter.getBalance()),
        "Sender balance should be rolled back");

    assertEquals(
        0,
        new BigDecimal("300.00")
            .compareTo(receiverAfter.getBalance()),
        "Receiver balance should be rolled back");

    // No partial transfer or transaction records should remain.
    assertEquals(
        0,
        transferRepository.count(),
        "Transfer record should be rolled back");

    assertEquals(
        0,
        transactionRepository.count(),
        "Both transaction records should be rolled back");
  }

  private void clearDatabase() {
    transactionRepository.deleteAll();
    transferRepository.deleteAll();
    walletRepository.deleteAll();
  }
}