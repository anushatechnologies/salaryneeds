package com.salaryneeds.repository;

import com.salaryneeds.entity.WorkerBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerBankAccountRepository extends JpaRepository<WorkerBankAccount, String> {
    List<WorkerBankAccount> findByWorkerId(String workerId);
    Optional<WorkerBankAccount> findByWorkerIdAndIsPrimaryTrue(String workerId);
}
