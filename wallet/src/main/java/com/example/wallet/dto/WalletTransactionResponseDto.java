
package com.example.wallet.dto;

import com.example.wallet.entity.TransactionType;
import com.example.wallet.entity.WalletTransaction;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class WalletTransactionResponseDto {

  private Long id;
  private Long walletId;
  private TransactionType transactionType;
  private BigDecimal amount;
  private BigDecimal balanceAfter;
  private String description;
  private LocalDateTime createdAt;

  public static WalletTransactionResponseDto fromEntity(
      WalletTransaction transaction) {

    return WalletTransactionResponseDto.builder()
        .id(transaction.getId())
        .walletId(transaction.getWalletId())
        .transactionType(transaction.getTransactionType())
        .amount(transaction.getAmount())
        .balanceAfter(transaction.getBalanceAfter())
        .description(transaction.getDescription())
        .createdAt(transaction.getCreatedAt())
        .build();
  }
}
