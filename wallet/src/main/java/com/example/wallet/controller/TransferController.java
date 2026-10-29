package com.example.wallet.controller;

import com.example.wallet.dto.TransferHistoryFilterDto;
import com.example.wallet.dto.TransferHistoryResponseDto;
import com.example.wallet.dto.TransferRequestDto;
import com.example.wallet.dto.TransferResponseDto;
import com.example.wallet.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

  private final TransferService transferService;

  @PostMapping
  public ResponseEntity<TransferResponseDto> transferMoney(
      @Valid @RequestBody TransferRequestDto request) {

    TransferResponseDto response = transferService.transferMoney(request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  @GetMapping
  public ResponseEntity<Page<TransferHistoryResponseDto>> getTransferHistory(
      @RequestParam("userId") Long userId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    TransferHistoryFilterDto filters = TransferHistoryFilterDto.builder()
        .status(status)
        .startDate(startDate)
        .endDate(endDate)
        .build();

    Page<TransferHistoryResponseDto> history = transferService.getTransferHistory(
        userId, filters, page, size);

    return ResponseEntity.ok(history);
  }
}