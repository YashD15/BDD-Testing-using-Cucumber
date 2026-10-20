
package com.example.wallet.controller;

import com.example.wallet.dto.CreateWalletRequestDto;
import com.example.wallet.dto.WalletResponseDto;
import com.example.wallet.service.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    public ResponseEntity<WalletResponseDto> createWallet(
            @Valid @RequestBody CreateWalletRequestDto request) {

        WalletResponseDto response = walletService.createWallet(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<WalletResponseDto> getWalletByUserId(
            @RequestParam("userId") @Positive(message = "User ID must be greater than zero") Long userId) {

        WalletResponseDto response = walletService.getWalletByUserId(userId);

        return ResponseEntity.ok(response);
    }
}
