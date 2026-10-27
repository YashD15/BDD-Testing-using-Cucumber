
package com.example.wallet.repository;

import com.example.wallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserIdAndIsDeletedFalse(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT w
            FROM Wallet w
            WHERE w.userId = :userId
              AND w.isDeleted = false
            """)
    Optional<Wallet> findActiveWalletByUserIdForUpdate(
            @Param("userId") Long userId);
}
