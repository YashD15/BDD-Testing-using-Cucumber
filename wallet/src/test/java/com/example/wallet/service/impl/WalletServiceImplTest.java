
package com.example.wallet.service.impl;

import com.example.wallet.dto.CreateWalletRequestDto;
import com.example.wallet.dto.WalletResponseDto;
import com.example.wallet.entity.Wallet;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

        @Mock
        private WalletRepository walletRepository;

        @InjectMocks
        private WalletServiceImpl walletService;

        @Test
        void createWalletShouldCreateWalletWithZeroBalance() {

                CreateWalletRequestDto request = new CreateWalletRequestDto();
                request.setUserId(1L);
                request.setCurrency("INR");

                when(walletRepository.findByUserIdAndIsDeletedFalse(1L))
                                .thenReturn(Optional.empty());

                when(walletRepository.save(any(Wallet.class)))
                                .thenAnswer(invocation -> {
                                        Wallet wallet = invocation.getArgument(0);
                                        wallet.setId(10L);
                                        return wallet;
                                });

                WalletResponseDto response = walletService.createWallet(request);

                assertNotNull(response);
                assertEquals(10L, response.getId());
                assertEquals(1L, response.getUserId());
                assertEquals(0, response.getBalance().compareTo(BigDecimal.ZERO));
                assertEquals("INR", response.getCurrency());

                verify(walletRepository)
                                .findByUserIdAndIsDeletedFalse(1L);
                verify(walletRepository).save(any(Wallet.class));
        }

        @Test
        void createWalletShouldRejectDuplicateWallet() {

                CreateWalletRequestDto request = new CreateWalletRequestDto();
                request.setUserId(1L);
                request.setCurrency("INR");

                Wallet existingWallet = Wallet.builder()
                                .id(10L)
                                .userId(1L)
                                .balance(BigDecimal.ZERO)
                                .currency("INR")
                                .build();

                when(walletRepository.findByUserIdAndIsDeletedFalse(1L))
                                .thenReturn(Optional.of(existingWallet));

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> walletService.createWallet(request));

                assertEquals(
                                "User already has an active wallet",
                                exception.getMessage());

                verify(walletRepository, never()).save(any(Wallet.class));
        }

        @Test
        void getWalletByUserIdShouldReturnWalletDetails() {

                Wallet existingWallet = Wallet.builder()
                                .id(10L)
                                .userId(1L)
                                .balance(new BigDecimal("250.00"))
                                .currency("INR")
                                .build();

                when(walletRepository.findByUserIdAndIsDeletedFalse(1L))
                                .thenReturn(Optional.of(existingWallet));

                WalletResponseDto response = walletService.getWalletByUserId(1L);

                assertNotNull(response);
                assertEquals(10L, response.getId());
                assertEquals(1L, response.getUserId());
                assertEquals(
                                0,
                                response.getBalance().compareTo(new BigDecimal("250.00")));
                assertEquals("INR", response.getCurrency());

                verify(walletRepository)
                                .findByUserIdAndIsDeletedFalse(1L);
        }

        @Test
        void getWalletByUserIdShouldThrowWhenWalletDoesNotExist() {

                when(walletRepository.findByUserIdAndIsDeletedFalse(9999L))
                                .thenReturn(Optional.empty());

                WalletNotFoundException exception = assertThrows(
                                WalletNotFoundException.class,
                                () -> walletService.getWalletByUserId(9999L));

                assertEquals(
                                "Wallet not found for user ID: 9999",
                                exception.getMessage());

                verify(walletRepository)
                                .findByUserIdAndIsDeletedFalse(9999L);
        }

}
