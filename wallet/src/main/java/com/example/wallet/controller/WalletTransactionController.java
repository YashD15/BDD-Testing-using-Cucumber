
package com.example.wallet.controller;

import com.example.wallet.dto.CreateTransactionRequestDto;
import com.example.wallet.dto.WalletTransactionResponseDto;
import com.example.wallet.service.WalletTransactionService;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@Validated
public class WalletTransactionController {

  private final WalletTransactionService transactionService;

  public WalletTransactionController(
      WalletTransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PostMapping
  public ResponseEntity<WalletTransactionResponseDto> createTransaction(
      @Valid @RequestBody CreateTransactionRequestDto request) {

    WalletTransactionResponseDto response = transactionService.createTransaction(request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  @GetMapping
  public ResponseEntity<Page<WalletTransactionResponseDto>> getTransactionsByUserId(
      @RequestParam("userId") Long userId,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size) {

    Page<WalletTransactionResponseDto> transactions = transactionService.getTransactionsByUserId(
        userId, page, size);

    return ResponseEntity.ok(transactions);
  }
}
