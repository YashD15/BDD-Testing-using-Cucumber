package com.example.wallet.controller;

import com.example.wallet.dto.CreateWalletRequestDto;
import com.example.wallet.dto.WalletResponseDto;
import com.example.wallet.exception.GlobalExceptionHandler;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.service.WalletService;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
@Import(GlobalExceptionHandler.class)
class WalletControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private WalletService walletService;

  @Test
  void createWalletShouldReturn201() throws Exception {

    CreateWalletRequestDto request = new CreateWalletRequestDto();
    request.setUserId(1L);
    request.setCurrency("INR");

    WalletResponseDto response = WalletResponseDto.builder()
        .id(10L)
        .userId(1L)
        .balance(BigDecimal.ZERO)
        .currency("INR")
        .build();

    when(walletService.createWallet(any(CreateWalletRequestDto.class)))
        .thenReturn(response);

    mockMvc.perform(post("/api/wallets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.currency").value("INR"));
  }

  @Test
  void getWalletShouldReturn200() throws Exception {

    WalletResponseDto response = WalletResponseDto.builder()
        .id(10L)
        .userId(1L)
        .balance(new BigDecimal("500.00"))
        .currency("INR")
        .build();

    when(walletService.getWalletByUserId(1L))
        .thenReturn(response);

    mockMvc.perform(get("/api/wallets")
        .param("userId", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.currency").value("INR"));
  }

  @Test
  void deleteWalletShouldReturn204() throws Exception {

    doNothing().when(walletService).deleteWallet(1L);

    mockMvc.perform(delete("/api/wallets/1"))
        .andExpect(status().isNoContent());

    verify(walletService).deleteWallet(1L);
  }

  @Test
  void deleteWalletShouldReturn404WhenWalletDoesNotExist()
      throws Exception {

    doThrow(new WalletNotFoundException(
        "Wallet not found for user ID: 9999"))
        .when(walletService).deleteWallet(9999L);

    mockMvc.perform(delete("/api/wallets/9999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message")
            .value("Wallet not found for user ID: 9999"));
  }

  @Test
  void deleteWalletShouldReturn400WhenUserIdIsInvalid()
      throws Exception {

    doThrow(new IllegalArgumentException(
        "User ID must be a positive number"))
        .when(walletService).deleteWallet(0L);

    mockMvc.perform(delete("/api/wallets/0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message")
            .value("User ID must be a positive number"));
  }
}