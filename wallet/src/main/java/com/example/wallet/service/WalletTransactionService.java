
package com.example.wallet.service;

import com.example.wallet.dto.CreateTransactionRequestDto;
import com.example.wallet.dto.WalletTransactionResponseDto;

import java.util.List;

public interface WalletTransactionService {

  WalletTransactionResponseDto createTransaction(
      CreateTransactionRequestDto request);

  List<WalletTransactionResponseDto> getTransactionsByUserId(
      Long userId);
}
