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
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

        @Mock
        private WalletRepository walletRepository;

        @Mock
        private WalletTransactionRepository transactionRepository;

        @Mock
        private WalletTransferRepository transferRepository;

        @InjectMocks
        private TransferServiceImpl transferService;

        private Wallet senderWallet;
        private Wallet receiverWallet;

        @BeforeEach
        void setUp() {
                senderWallet = Wallet.builder()
                                .id(1L)
                                .userId(101L)
                                .balance(new BigDecimal("1000.00"))
                                .currency("INR")
                                .isDeleted(false)
                                .build();

                receiverWallet = Wallet.builder()
                                .id(2L)
                                .userId(102L)
                                .balance(new BigDecimal("300.00"))
                                .currency("INR")
                                .isDeleted(false)
                                .build();
        }

        @Test
        void transferMoney_shouldTransferSuccessfully() {
                TransferRequestDto request = TransferRequestDto.builder()
                                .senderUserId(101L)
                                .receiverUserId(102L)
                                .amount(new BigDecimal("200.00"))
                                .description("Test transfer")
                                .build();

                when(walletRepository.findActiveWalletByUserIdForUpdate(101L))
                                .thenReturn(Optional.of(senderWallet));

                when(walletRepository.findActiveWalletByUserIdForUpdate(102L))
                                .thenReturn(Optional.of(receiverWallet));

                when(transferRepository.saveAndFlush(any(WalletTransfer.class)))
                                .thenAnswer(invocation -> {
                                        WalletTransfer transfer = invocation.getArgument(0);
                                        transfer.setId(1L);
                                        return transfer;
                                });

                TransferResponseDto response = transferService.transferMoney(request);

                assertEquals(
                                0,
                                new BigDecimal("800.00")
                                                .compareTo(senderWallet.getBalance()));

                assertEquals(
                                0,
                                new BigDecimal("500.00")
                                                .compareTo(receiverWallet.getBalance()));

                assertEquals("COMPLETED", response.getStatus());
                assertEquals(1L, response.getTransferId());

                verify(transferRepository).saveAndFlush(any(WalletTransfer.class));
                verify(transactionRepository, times(2))
                                .save(any(WalletTransaction.class));
        }

        @Test
        void transferMoney_shouldRejectInsufficientFunds() {
                senderWallet.setBalance(new BigDecimal("100.00"));

                TransferRequestDto request = TransferRequestDto.builder()
                                .senderUserId(101L)
                                .receiverUserId(102L)
                                .amount(new BigDecimal("200.00"))
                                .build();

                when(walletRepository.findActiveWalletByUserIdForUpdate(101L))
                                .thenReturn(Optional.of(senderWallet));

                when(walletRepository.findActiveWalletByUserIdForUpdate(102L))
                                .thenReturn(Optional.of(receiverWallet));

                assertThrows(
                                IllegalArgumentException.class,
                                () -> transferService.transferMoney(request));

                assertEquals(
                                0,
                                new BigDecimal("100.00")
                                                .compareTo(senderWallet.getBalance()));

                assertEquals(
                                0,
                                new BigDecimal("300.00")
                                                .compareTo(receiverWallet.getBalance()));

                verify(transferRepository, never())
                                .saveAndFlush(any(WalletTransfer.class));

                verify(transactionRepository, never())
                                .save(any(WalletTransaction.class));
        }

        @Test
        void transferMoney_shouldRejectSameUser() {
                TransferRequestDto request = TransferRequestDto.builder()
                                .senderUserId(101L)
                                .receiverUserId(101L)
                                .amount(new BigDecimal("100.00"))
                                .build();

                assertThrows(
                                IllegalArgumentException.class,
                                () -> transferService.transferMoney(request));

                verify(walletRepository, never())
                                .findActiveWalletByUserIdForUpdate(any());

                verify(transferRepository, never())
                                .saveAndFlush(any(WalletTransfer.class));
        }

        @Test
        void transferMoney_shouldRejectWhenSenderWalletDoesNotExist() {
                TransferRequestDto request = TransferRequestDto.builder()
                                .senderUserId(101L)
                                .receiverUserId(102L)
                                .amount(new BigDecimal("100.00"))
                                .build();

                when(walletRepository.findActiveWalletByUserIdForUpdate(101L))
                                .thenReturn(Optional.empty());

                assertThrows(
                                WalletNotFoundException.class,
                                () -> transferService.transferMoney(request));

                verify(transferRepository, never())
                                .saveAndFlush(any(WalletTransfer.class));
        }

        @Test
        void transferMoney_shouldRejectDifferentCurrencies() {
                receiverWallet.setCurrency("USD");

                TransferRequestDto request = TransferRequestDto.builder()
                                .senderUserId(101L)
                                .receiverUserId(102L)
                                .amount(new BigDecimal("100.00"))
                                .build();

                when(walletRepository.findActiveWalletByUserIdForUpdate(101L))
                                .thenReturn(Optional.of(senderWallet));

                when(walletRepository.findActiveWalletByUserIdForUpdate(102L))
                                .thenReturn(Optional.of(receiverWallet));

                assertThrows(
                                IllegalArgumentException.class,
                                () -> transferService.transferMoney(request));

                verify(transferRepository, never())
                                .saveAndFlush(any(WalletTransfer.class));
        }

        @Test
        void transferMoney_shouldRejectNullRequest() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> transferService.transferMoney(null));

                verify(transferRepository, never())
                                .saveAndFlush(any(WalletTransfer.class));
        }

        @Test
        void getTransferHistory_shouldReturnPaginatedHistory() {
                LocalDateTime now = LocalDateTime.now();

                WalletTransfer transfer = WalletTransfer.builder()
                                .id(10L)
                                .senderWalletId(1L)
                                .receiverWalletId(2L)
                                .amount(new BigDecimal("200.00"))
                                .currency("INR")
                                .status("COMPLETED")
                                .description("Test transfer")
                                .createdAt(now)
                                .build();

                Pageable pageable = PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                                Sort.Order.desc("createdAt"),
                                                Sort.Order.desc("id")));

                when(walletRepository.findByUserIdAndIsDeletedFalse(101L))
                                .thenReturn(Optional.of(senderWallet));

                when(transferRepository.findTransferHistoryByWalletId(
                                1L, null, null, null, pageable))
                                .thenReturn(new PageImpl<>(List.of(transfer), pageable, 1));

                when(walletRepository.findById(1L))
                                .thenReturn(Optional.of(senderWallet));

                when(walletRepository.findById(2L))
                                .thenReturn(Optional.of(receiverWallet));

                Page<TransferHistoryResponseDto> result = transferService.getTransferHistory(
                                101L,
                                TransferHistoryFilterDto.builder().build(),
                                0,
                                10);

                assertEquals(1, result.getTotalElements());
                assertEquals(1, result.getTotalPages());
                assertEquals(10, result.getSize());

                TransferHistoryResponseDto item = result.getContent().get(0);

                assertEquals(10L, item.getTransferId());
                assertEquals(101L, item.getSenderUserId());
                assertEquals(102L, item.getReceiverUserId());
                assertEquals("COMPLETED", item.getStatus());
        }

        @Test
        void getTransferHistory_shouldPassStatusAndDateFiltersToRepository() {
                LocalDate startDate = LocalDate.of(2026, 10, 1);
                LocalDate endDate = LocalDate.of(2026, 10, 31);

                TransferHistoryFilterDto filters = TransferHistoryFilterDto.builder()
                                .status("completed")
                                .startDate(startDate)
                                .endDate(endDate)
                                .build();

                Pageable pageable = PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                                Sort.Order.desc("createdAt"),
                                                Sort.Order.desc("id")));

                when(walletRepository.findByUserIdAndIsDeletedFalse(101L))
                                .thenReturn(Optional.of(senderWallet));

                when(transferRepository.findTransferHistoryByWalletId(
                                1L,
                                "COMPLETED",
                                startDate.atStartOfDay(),
                                endDate.plusDays(1).atStartOfDay(),
                                pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

                Page<TransferHistoryResponseDto> result = transferService.getTransferHistory(
                                101L, filters, 0, 10);

                assertEquals(0, result.getTotalElements());

                verify(transferRepository).findTransferHistoryByWalletId(
                                1L,
                                "COMPLETED",
                                startDate.atStartOfDay(),
                                endDate.plusDays(1).atStartOfDay(),
                                pageable);
        }

        @Test
        void getTransferHistory_shouldRejectUnsupportedStatus() {
                TransferHistoryFilterDto filters = TransferHistoryFilterDto.builder()
                                .status("UNKNOWN")
                                .build();

                assertThrows(
                                IllegalArgumentException.class,
                                () -> transferService.getTransferHistory(
                                                101L, filters, 0, 10));

                verify(walletRepository, never())
                                .findByUserIdAndIsDeletedFalse(any());
        }

        @Test
        void getTransferHistory_shouldRejectStartDateAfterEndDate() {
                TransferHistoryFilterDto filters = TransferHistoryFilterDto.builder()
                                .startDate(LocalDate.of(2026, 10, 31))
                                .endDate(LocalDate.of(2026, 10, 1))
                                .build();

                assertThrows(
                                IllegalArgumentException.class,
                                () -> transferService.getTransferHistory(
                                                101L, filters, 0, 10));
        }

        @Test
        void getTransferHistory_shouldRejectNegativePage() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> transferService.getTransferHistory(
                                                101L,
                                                TransferHistoryFilterDto.builder().build(),
                                                -1,
                                                10));
        }

        @Test
        void getTransferHistory_shouldRejectInvalidPageSize() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> transferService.getTransferHistory(
                                                101L,
                                                TransferHistoryFilterDto.builder().build(),
                                                0,
                                                101));
        }

        @Test
        void getTransferHistory_shouldRejectMissingWallet() {
                when(walletRepository.findByUserIdAndIsDeletedFalse(999L))
                                .thenReturn(Optional.empty());

                assertThrows(
                                WalletNotFoundException.class,
                                () -> transferService.getTransferHistory(
                                                999L,
                                                TransferHistoryFilterDto.builder().build(),
                                                0,
                                                10));
        }
}