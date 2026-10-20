
package com.example.wallet.service.impl;

import com.example.wallet.dto.CreateWalletRequestDto;
import com.example.wallet.dto.WalletResponseDto;
import com.example.wallet.entity.Wallet;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional
    public WalletResponseDto createWallet(CreateWalletRequestDto request) {

        // Prevent duplicate wallets for the same user.
        if (walletRepository
                .findByUserIdAndIsDeletedFalse(request.getUserId())
                .isPresent()) {
            throw new IllegalArgumentException(
                    "User already has an active wallet");
        }

        Wallet wallet = Wallet.builder()
                .userId(request.getUserId())
                .balance(BigDecimal.ZERO)
                .currency(request.getCurrency())
                .build();

        Wallet savedWallet = walletRepository.save(wallet);

        return WalletResponseDto.fromEntity(savedWallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDto getWalletByUserId(Long userId) {

        Wallet wallet = walletRepository
                .findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new WalletNotFoundException(
                        "Wallet not found for user ID: " + userId));

        return WalletResponseDto.fromEntity(wallet);
    }
}
