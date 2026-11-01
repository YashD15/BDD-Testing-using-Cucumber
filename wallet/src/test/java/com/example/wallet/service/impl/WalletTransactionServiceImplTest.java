package com.example.wallet.service.impl;

import com.example.wallet.dto.CreateTransactionRequestDto;
import com.example.wallet.dto.WalletTransactionResponseDto;
import com.example.wallet.entity.Wallet;
import com.example.wallet.entity.WalletTransaction;
import com.example.wallet.entity.TransactionType;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        void getTransactionsShouldReturnPaginatedHistoryForUser() {
                WalletTransaction transaction = WalletTransaction.builder()
                                .id(100L)
                                .walletId(10L)
                                .transactionType(TransactionType.CREDIT)
                                .amount(new BigDecimal("500.00"))
                                .balanceAfter(new BigDecimal("500.00"))
                                .description("Test deposit")
                                .build();

                Page<WalletTransaction> transactionPage = new PageImpl<>(
                                List.of(transaction),
                                PageRequest.of(0, 10),
                                1);

                when(walletRepository.findByUserIdAndIsDeletedFalse(1L))
                                .thenReturn(Optional.of(wallet));

                when(transactionRepository
                                .findByWalletIdOrderByCreatedAtDescIdDesc(
                                                eq(10L),
                                                any(Pageable.class)))
                                .thenReturn(transactionPage);

                Page<WalletTransactionResponseDto> response = transactionService.getTransactionsByUserId(1L, 0, 10);

                assertEquals(1, response.getContent().size());
                assertEquals(1, response.getTotalElements());
                assertEquals(1, response.getTotalPages());
                assertEquals(0, response.getNumber());
                assertEquals(10, response.getSize());

                WalletTransactionResponseDto dto = response.getContent().get(0);

                assertEquals(100L, dto.getId());
                assertEquals(TransactionType.CREDIT, dto.getTransactionType());
                assertEquals(
                                new BigDecimal("500.00"),
                                dto.getBalanceAfter());
        }

        @Test
        void getTransactionsShouldRejectNegativePage() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> transactionService.getTransactionsByUserId(1L, -1, 10));

                verify(walletRepository, never())
                                .findByUserIdAndIsDeletedFalse(anyLong());
        }

        @Test
        void getTransactionsShouldRejectInvalidPageSize() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> transactionService.getTransactionsByUserId(1L, 0, 101));

                verify(walletRepository, never())
                                .findByUserIdAndIsDeletedFalse(anyLong());
        }

        @Test
        void getTransactionsShouldFailWhenWalletDoesNotExist() {
                when(walletRepository.findByUserIdAndIsDeletedFalse(999L))
                                .thenReturn(Optional.empty());

                assertThrows(
                                WalletNotFoundException.class,
                                () -> transactionService.getTransactionsByUserId(999L, 0, 10));
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