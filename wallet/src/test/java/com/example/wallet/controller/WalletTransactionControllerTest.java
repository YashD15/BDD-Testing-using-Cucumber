
package com.example.wallet.controller;

import com.example.wallet.dto.CreateTransactionRequestDto;
import com.example.wallet.dto.WalletTransactionResponseDto;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.exception.GlobalExceptionHandler;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.service.WalletTransactionService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletTransactionController.class)
@Import(GlobalExceptionHandler.class)
class WalletTransactionControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private WalletTransactionService transactionService;

        @Test
        void createTransactionShouldReturn201() throws Exception {
                CreateTransactionRequestDto request = new CreateTransactionRequestDto();
                request.setUserId(1L);
                request.setTransactionType(TransactionType.CREDIT);
                request.setAmount(new BigDecimal("100.00"));
                request.setDescription("Test deposit");

                WalletTransactionResponseDto response = WalletTransactionResponseDto.builder()
                                .id(10L)
                                .walletId(5L)
                                .transactionType(TransactionType.CREDIT)
                                .amount(new BigDecimal("100.00"))
                                .balanceAfter(new BigDecimal("100.00"))
                                .description("Test deposit")
                                .createdAt(LocalDateTime.now())
                                .build();

                when(transactionService.createTransaction(any()))
                                .thenReturn(response);

                mockMvc.perform(post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(10))
                                .andExpect(jsonPath("$.transactionType").value("CREDIT"))
                                .andExpect(jsonPath("$.balanceAfter").value(100.00));
        }

        @Test
        void createTransactionShouldReturn400ForInvalidAmount()
                        throws Exception {
                String request = """
                                {
                                  "userId": 1,
                                  "transactionType": "CREDIT",
                                  "amount": 0,
                                  "description": "Invalid deposit"
                                }
                                """;

                mockMvc.perform(post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void getTransactionsShouldReturn200() throws Exception {
                WalletTransactionResponseDto transaction = WalletTransactionResponseDto.builder()
                                .id(10L)
                                .walletId(5L)
                                .transactionType(TransactionType.CREDIT)
                                .amount(new BigDecimal("100.00"))
                                .balanceAfter(new BigDecimal("100.00"))
                                .description("Test deposit")
                                .createdAt(LocalDateTime.now())
                                .build();

                Page<WalletTransactionResponseDto> transactionPage = new PageImpl<>(
                                List.of(transaction),
                                PageRequest.of(0, 10),
                                1);

                when(transactionService.getTransactionsByUserId(
                                eq(1L), eq(0), eq(10)))
                                .thenReturn(transactionPage);

                mockMvc.perform(get("/api/transactions")
                                .param("userId", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(10))
                                .andExpect(jsonPath("$.content[0].transactionType")
                                                .value("CREDIT"))
                                .andExpect(jsonPath("$.totalElements").value(1))
                                .andExpect(jsonPath("$.totalPages").value(1))
                                .andExpect(jsonPath("$.number").value(0))
                                .andExpect(jsonPath("$.size").value(10));
        }

        @Test
        void getTransactionsShouldReturn404WhenWalletDoesNotExist()
                        throws Exception {

                when(transactionService.getTransactionsByUserId(
                                999L, 0, 10))
                                .thenThrow(new WalletNotFoundException(
                                                "Wallet not found for user ID: 999"));

                mockMvc.perform(get("/api/transactions")
                                .param("userId", "999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message")
                                                .value("Wallet not found for user ID: 999"))
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Not Found"));
        }

        @Test
        void createTransactionShouldRejectMoreThanTwoDecimalPlaces() throws Exception {
                mockMvc.perform(post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "userId": 101,
                                                    "transactionType": "CREDIT",
                                                    "amount": 10.555,
                                                    "description": "Invalid precision test"
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message")
                                                .value("Transaction amount must have at most 2 decimal places"));
        }
}
