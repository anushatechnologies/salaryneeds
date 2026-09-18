package com.salaryneeds.repository;

import com.salaryneeds.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, String> {
    List<WalletTransaction> findByWorkerIdOrderByCreatedAtDesc(String workerId);
}
