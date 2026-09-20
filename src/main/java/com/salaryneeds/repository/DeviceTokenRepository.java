package com.salaryneeds.repository;

import com.salaryneeds.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, String> {
    Optional<DeviceToken> findByWorkerId(String workerId);
    Optional<DeviceToken> findByToken(String token);
}
