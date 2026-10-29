package com.example.wallet.service;

import com.example.wallet.dto.TransferHistoryFilterDto;
import com.example.wallet.dto.TransferHistoryResponseDto;
import com.example.wallet.dto.TransferRequestDto;
import com.example.wallet.dto.TransferResponseDto;
import org.springframework.data.domain.Page;

public interface TransferService {

  TransferResponseDto transferMoney(TransferRequestDto request);

  Page<TransferHistoryResponseDto> getTransferHistory(
      Long userId,
      TransferHistoryFilterDto filters,
      int page,
      int size);
}