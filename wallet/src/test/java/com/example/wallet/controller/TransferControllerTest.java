
package com.example.wallet.controller;

import com.example.wallet.dto.TransferHistoryFilterDto;
import com.example.wallet.dto.TransferHistoryResponseDto;
import com.example.wallet.dto.TransferRequestDto;
import com.example.wallet.dto.TransferResponseDto;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(TransferController.class)
class TransferControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private TransferService transferService;

  @Test
  void shouldReturnCreatedWhenTransferSucceeds() throws Exception {
    TransferRequestDto request = new TransferRequestDto();
    request.setSenderUserId(1L);
    request.setReceiverUserId(2L);
    request.setAmount(new BigDecimal("200.00"));
    request.setDescription("Test transfer");

    TransferResponseDto response = TransferResponseDto.builder()
        .transferId(100L)
        .senderUserId(1L)
        .receiverUserId(2L)
        .amount(new BigDecimal("200.00"))
        .currency("INR")
        .senderBalanceAfter(new BigDecimal("800.00"))
        .receiverBalanceAfter(new BigDecimal("500.00"))
        .createdAt(LocalDateTime.now())
        .status("SUCCESS")
        .build();

    when(transferService.transferMoney(any(TransferRequestDto.class)))
        .thenReturn(response);

    mockMvc.perform(post("/api/transfers")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.transferId").value(100))
        .andExpect(jsonPath("$.senderUserId").value(1))
        .andExpect(jsonPath("$.receiverUserId").value(2))
        .andExpect(jsonPath("$.currency").value("INR"))
        .andExpect(jsonPath("$.status").value("SUCCESS"));
  }

  @Test
  void shouldReturnBadRequestWhenAmountIsZero() throws Exception {
    TransferRequestDto request = new TransferRequestDto();
    request.setSenderUserId(1L);
    request.setReceiverUserId(2L);
    request.setAmount(BigDecimal.ZERO);

    mockMvc.perform(post("/api/transfers")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnBadRequestWhenUserIdsAreMissing() throws Exception {
    String request = """
        {
          "amount": 100.00,
          "description": "Test transfer"
        }
        """;

    mockMvc.perform(post("/api/transfers")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getTransferHistory_shouldReturnHistory() throws Exception {
    TransferHistoryResponseDto historyItem = TransferHistoryResponseDto.builder()
        .transferId(1L)
        .senderUserId(101L)
        .receiverUserId(102L)
        .amount(new BigDecimal("200.00"))
        .currency("INR")
        .status("COMPLETED")
        .description("Test transfer")
        .build();

    Pageable pageable = PageRequest.of(
        0, 10,
        Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")));

    Page<TransferHistoryResponseDto> history = new PageImpl<>(
        List.of(historyItem),
        pageable,
        1);

    when(transferService.getTransferHistory(
        eq(101L),
        argThat(filters -> filters.getStatus() == null
            && filters.getStartDate() == null
            && filters.getEndDate() == null),
        eq(0),
        eq(10)))
        .thenReturn(history);

    mockMvc.perform(get("/api/transfers")
        .param("userId", "101"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].transferId").value(1))
        .andExpect(jsonPath("$.content[0].senderUserId").value(101))
        .andExpect(jsonPath("$.content[0].receiverUserId").value(102))
        .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.number").value(0))
        .andExpect(jsonPath("$.size").value(10));
  }

  @Test
  void getTransferHistory_shouldRespectPageAndSize() throws Exception {
    Pageable pageable = PageRequest.of(
        1,
        5,
        Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")));

    Page<TransferHistoryResponseDto> history = new PageImpl<>(List.of(), pageable, 12);

    when(transferService.getTransferHistory(
        eq(101L),
        any(TransferHistoryFilterDto.class),
        eq(1),
        eq(5)))
        .thenReturn(history);

    mockMvc.perform(get("/api/transfers")
        .param("userId", "101")
        .param("page", "1")
        .param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.totalElements").value(12))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.number").value(1))
        .andExpect(jsonPath("$.size").value(5));
  }

  @Test
  void getTransferHistory_shouldReturnEmptyPageWhenNoTransfersExist()
      throws Exception {

    Pageable pageable = PageRequest.of(
        0,
        10,
        Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")));

    when(transferService.getTransferHistory(
        eq(101L),
        argThat(filters -> filters.getStatus() == null
            && filters.getStartDate() == null
            && filters.getEndDate() == null),
        eq(0),
        eq(10)))
        .thenReturn(new PageImpl<>(List.of(), pageable, 0));

    mockMvc.perform(get("/api/transfers")
        .param("userId", "101"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.totalPages").value(0));
  }

  @Test
  void getTransferHistory_shouldReturnNotFoundWhenWalletDoesNotExist()
      throws Exception {

    when(transferService.getTransferHistory(
        eq(999L),
        any(TransferHistoryFilterDto.class),
        eq(0),
        eq(10)))
        .thenThrow(new WalletNotFoundException(
            "Wallet not found for user ID: 999"));

    mockMvc.perform(get("/api/transfers")
        .param("userId", "999"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getTransferHistory_shouldPassFiltersToService() throws Exception {
    Pageable pageable = PageRequest.of(
        0,
        10,
        Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")));

    Page<TransferHistoryResponseDto> history = new PageImpl<>(List.of(), pageable, 0);

    when(transferService.getTransferHistory(
        eq(101L),
        argThat(filters -> "COMPLETED".equals(filters.getStatus())
            && LocalDate.of(2026, 10, 1)
                .equals(filters.getStartDate())
            && LocalDate.of(2026, 10, 31)
                .equals(filters.getEndDate())),
        eq(0),
        eq(10)))
        .thenReturn(history);

    mockMvc.perform(get("/api/transfers")
        .param("userId", "101")
        .param("status", "COMPLETED")
        .param("startDate", "2026-10-01")
        .param("endDate", "2026-10-31"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.totalElements").value(0));
  }

}
