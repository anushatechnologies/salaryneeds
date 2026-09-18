package com.salaryneeds.repository;

import com.salaryneeds.entity.WorkerWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerWalletRepository extends JpaRepository<WorkerWallet, String> {
    Optional<WorkerWallet> findByWorkerId(String workerId);
    List<WorkerWallet> findByEarningsBalanceGreaterThanAndBankVerifiedTrue(BigDecimal minBalance);
}
