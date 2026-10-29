
package com.example.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDto {

  private Long transferId;

  private Long senderUserId;

  private Long receiverUserId;

  private BigDecimal amount;

  private String currency;

  private BigDecimal senderBalanceAfter;

  private BigDecimal receiverBalanceAfter;

  private LocalDateTime createdAt;

  private String status;
}
