package com.example.wallet.repository;

import com.example.wallet.entity.WalletTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WalletTransferRepository
      extends JpaRepository<WalletTransfer, Long> {

   List<WalletTransfer> findBySenderWalletIdOrderByCreatedAtDesc(
         Long senderWalletId);

   List<WalletTransfer> findByReceiverWalletIdOrderByCreatedAtDesc(
         Long receiverWalletId);

   @Query(value = """
         SELECT t
         FROM WalletTransfer t
         WHERE (t.senderWalletId = :walletId
            OR t.receiverWalletId = :walletId)
           AND (:status IS NULL OR t.status = :status)
           AND (:startDate IS NULL OR t.createdAt >= :startDate)
           AND (:endDate IS NULL OR t.createdAt < :endDate)
         ORDER BY t.createdAt DESC, t.id DESC
         """, countQuery = """
         SELECT COUNT(t)
         FROM WalletTransfer t
         WHERE (t.senderWalletId = :walletId
            OR t.receiverWalletId = :walletId)
           AND (:status IS NULL OR t.status = :status)
           AND (:startDate IS NULL OR t.createdAt >= :startDate)
           AND (:endDate IS NULL OR t.createdAt < :endDate)
         """)
   Page<WalletTransfer> findTransferHistoryByWalletId(
         @Param("walletId") Long walletId,
         @Param("status") String status,
         @Param("startDate") LocalDateTime startDate,
         @Param("endDate") LocalDateTime endDate,
         Pageable pageable);
}