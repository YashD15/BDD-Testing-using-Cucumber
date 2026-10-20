
package com.example.wallet.service;

import com.example.wallet.dto.CreateWalletRequestDto;
import com.example.wallet.dto.WalletResponseDto;

public interface WalletService {

    WalletResponseDto createWallet(CreateWalletRequestDto request);

    WalletResponseDto getWalletByUserId(Long userId);
}
