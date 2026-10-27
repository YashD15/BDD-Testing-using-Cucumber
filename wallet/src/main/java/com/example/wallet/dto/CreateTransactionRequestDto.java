
package com.example.wallet.dto;

import com.example.wallet.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CreateTransactionRequestDto {

  @NotNull(message = "User ID is required")
  @Positive(message = "User ID must be greater than zero")
  private Long userId;

  @NotNull(message = "Transaction type is required")
  private TransactionType transactionType;

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
  private BigDecimal amount;

  @Size(max = 255, message = "Description cannot exceed 255 characters")
  private String description;
}
