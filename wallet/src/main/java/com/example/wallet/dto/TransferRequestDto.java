package com.example.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {

  @NotNull(message = "Sender user ID is required")
  @Positive(message = "Sender user ID must be positive")
  private Long senderUserId;

  @NotNull(message = "Receiver user ID is required")
  @Positive(message = "Receiver user ID must be positive")
  private Long receiverUserId;

  @NotNull(message = "Transfer amount is required")
  @DecimalMin(value = "0.01", message = "Transfer amount must be at least 0.01")
  private BigDecimal amount;

  @Size(max = 255, message = "Description cannot exceed 255 characters")
  private String description;
}