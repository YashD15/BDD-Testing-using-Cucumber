
package com.example.wallet.service;

import com.example.wallet.dto.CreateTransactionRequestDto;
import com.example.wallet.dto.WalletTransactionResponseDto;

import org.springframework.data.domain.Page;

public interface WalletTransactionService {

    WalletTransactionResponseDto createTransaction(
            CreateTransactionRequestDto request);

    Page<WalletTransactionResponseDto> getTransactionsByUserId(
            Long userId,
            int page,
            int size);

}
