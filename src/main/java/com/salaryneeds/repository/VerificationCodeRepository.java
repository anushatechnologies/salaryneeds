package com.salaryneeds.repository;

import com.salaryneeds.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findTopByPhoneAndConsumedFalseOrderByCreatedAtDesc(String phone);
}
