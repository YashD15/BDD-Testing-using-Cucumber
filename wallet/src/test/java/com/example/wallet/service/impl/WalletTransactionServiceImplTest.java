
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletTransactionServiceImplTest {

        @Mock
        private WalletRepository walletRepository;

        @Mock
        private WalletTransactionRepository transactionRepository;

        @InjectMocks
        private WalletTransactionServiceImpl transactionService;

        private Wallet wallet;

        @BeforeEach
        void setUp() {
                wallet = Wallet.builder()
                                .id(10L)
                                .userId(1L)
                                .balance(new BigDecimal("500.00"))
                                .currency("INR")
                                .build();
        }

        @Test
        void createCreditTransactionShouldIncreaseBalance() {
                wallet.setBalance(BigDecimal.ZERO);

                CreateTransactionRequestDto request = new CreateTransactionRequestDto();
                request.setUserId(1L);
                request.setTransactionType(TransactionType.CREDIT);
                request.setAmount(new BigDecimal("1000.00"));
                request.setDescription("Test deposit");

                when(walletRepository.findActiveWalletByUserIdForUpdate(1L))
                                .thenReturn(Optional.of(wallet));

                when(transactionRepository.save(any(WalletTransaction.class)))
                                .thenAnswer(invocation -> {
                                        WalletTransaction transaction = invocation.getArgument(0);
                                        transaction.setId(100L);
                                        return transaction;
                                });

                WalletTransactionResponseDto response = transactionService.createTransaction(request);

                assertEquals(TransactionType.CREDIT, response.getTransactionType());
                assertEquals(new BigDecimal("1000.00"), response.getAmount());
                assertEquals(new BigDecimal("1000.00"), response.getBalanceAfter());
                assertEquals(new BigDecimal("1000.00"), wallet.getBalance());
                assertEquals(100L, response.getId());

                verify(transactionRepository).save(any(WalletTransaction.class));
        }

        @Test
        void createDebitTransactionShouldDecreaseBalance() {
                CreateTransactionRequestDto request = new CreateTransactionRequestDto();
                request.setUserId(1L);
                request.setTransactionType(TransactionType.DEBIT);
                request.setAmount(new BigDecimal("200.00"));
                request.setDescription("Test withdrawal");

                when(walletRepository.findActiveWalletByUserIdForUpdate(1L))
                                .thenReturn(Optional.of(wallet));

                when(transactionRepository.save(any(WalletTransaction.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                WalletTransactionResponseDto response = transactionService.createTransaction(request);

                assertEquals(new BigDecimal("300.00"), wallet.getBalance());
                assertEquals(new BigDecimal("300.00"), response.getBalanceAfter());
                assertEquals(TransactionType.DEBIT, response.getTransactionType());

                verify(transactionRepository).save(any(WalletTransaction.class));
        }

        @Test
        void createDebitTransactionShouldRejectInsufficientBalance() {
                CreateTransactionRequestDto request = new CreateTransactionRequestDto();
                request.setUserId(1L);
                request.setTransactionType(TransactionType.DEBIT);
                request.setAmount(new BigDecimal("1000.00"));
                request.setDescription("Excessive withdrawal");

                when(walletRepository.findActiveWalletByUserIdForUpdate(1L))
                                .thenReturn(Optional.of(wallet));

                BusinessConflictException exception = assertThrows(
                                BusinessConflictException.class,
                                () -> transactionService.createTransaction(request));

                assertEquals("Insufficient wallet balance", exception.getMessage());
                assertEquals(new BigDecimal("500.00"), wallet.getBalance());

                verify(transactionRepository, never())
                                .save(any(WalletTransaction.class));
        }

        @Test
        void getTransactionsShouldReturnHistoryForUser() {
                WalletTransaction transaction = WalletTransaction.builder()
                                .id(100L)
                                .walletId(10L)
                                .transactionType(TransactionType.CREDIT)
                                .amount(new BigDecimal("500.00"))
                                .balanceAfter(new BigDecimal("500.00"))
                                .description("Test deposit")
                                .build();

                when(walletRepository.findByUserIdAndIsDeletedFalse(1L))
                                .thenReturn(Optional.of(wallet));

                when(transactionRepository.findByWalletIdOrderByCreatedAtDesc(10L))
                                .thenReturn(List.of(transaction));

                List<WalletTransactionResponseDto> response = transactionService.getTransactionsByUserId(1L);

                assertEquals(1, response.size());
                assertEquals(100L, response.get(0).getId());
                assertEquals(TransactionType.CREDIT, response.get(0).getTransactionType());
                assertEquals(new BigDecimal("500.00"), response.get(0).getBalanceAfter());
        }

        @Test
        void createTransactionShouldFailWhenWalletDoesNotExist() {
                CreateTransactionRequestDto request = new CreateTransactionRequestDto();
                request.setUserId(999L);
                request.setTransactionType(TransactionType.CREDIT);
                request.setAmount(new BigDecimal("100.00"));

                when(walletRepository.findActiveWalletByUserIdForUpdate(999L))
                                .thenReturn(Optional.empty());

                assertThrows(
                                WalletNotFoundException.class,
                                () -> transactionService.createTransaction(request));

                verify(transactionRepository, never())
                                .save(any(WalletTransaction.class));
        }
}
