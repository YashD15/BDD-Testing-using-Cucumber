package com.example.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferHistoryResponseDto {

  private Long transferId;

  private Long senderUserId;

  private Long receiverUserId;

  private BigDecimal amount;

  private String currency;

  private String status;

  private String description;

  private LocalDateTime createdAt;

}