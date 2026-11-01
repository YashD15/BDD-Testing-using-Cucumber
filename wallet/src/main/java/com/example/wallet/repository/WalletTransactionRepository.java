package com.example.wallet.repository;

import com.example.wallet.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface WalletTransactionRepository
    extends JpaRepository<WalletTransaction, Long> {

  Page<WalletTransaction> findByWalletIdOrderByCreatedAtDescIdDesc(
      Long walletId,
      Pageable pageable);

  @Transactional
  void deleteByWalletId(Long walletId);
}