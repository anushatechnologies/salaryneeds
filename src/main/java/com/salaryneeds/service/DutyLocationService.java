package com.salaryneeds.service;

import com.salaryneeds.dto.LocationHeartbeatRequest;
import com.salaryneeds.entity.LocationLog;
import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.entity.WorkerWallet;
import com.salaryneeds.exception.ApiException;
import com.salaryneeds.repository.LocationLogRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import com.salaryneeds.repository.WorkerWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DutyLocationService {

    private final WorkerProfileRepository workerProfileRepository;
    private final WorkerWalletRepository workerWalletRepository;
    private final LocationLogRepository locationLogRepository;

    @Transactional
    public Map<String, Object> toggleDuty(String workerId, Boolean requestedDuty) {
        if (workerId == null || workerId.isBlank()) {
            workerId = "w-default";
        }
        final String effectiveId = workerId;
        WorkerProfile profile = workerProfileRepository.findById(effectiveId).orElseGet(() ->
                WorkerProfile.builder()
                        .id(effectiveId)
                        .name("Worker " + effectiveId)
                        .phone("98765" + Math.abs(effectiveId.hashCode()) % 100000)
                        .trade("Technician")
                        .dutyOnline(false)
                        .build()
        );

        boolean targetDuty = requestedDuty != null ? requestedDuty : !Boolean.TRUE.equals(profile.getDutyOnline());

        if (targetDuty) {
            // Enforcement: Prepaid wallet balance >= ₹100
            WorkerWallet wallet = workerWalletRepository.findByWorkerId(profile.getId()).orElse(null);
            BigDecimal balance = wallet != null && wallet.getPrepaidDutyBalance() != null ? wallet.getPrepaidDutyBalance() : BigDecimal.valueOf(500.00);
            if (balance.compareTo(BigDecimal.valueOf(100.00)) < 0) {
                throw new ApiException("ERR_INSUFFICIENT_PREPAID_BALANCE",
                        "Minimum wallet balance of ₹100 is required to turn duty ON.",
                        HttpStatus.FORBIDDEN);
            }
        }

        profile.setDutyOnline(targetDuty);
        workerProfileRepository.save(profile);

        Map<String, Object> response = new HashMap<>();
        response.put("dutyOnline", targetDuty);
        response.put("message", targetDuty
                ? "You are now online and available for bookings"
                : "Duty status updated to Offline");
        return response;
    }

    @Transactional
    public Map<String, Object> recordHeartbeat(String workerId, LocationHeartbeatRequest request) {
        if (workerId == null || workerId.isBlank()) workerId = "w-default";

        WorkerProfile profile = workerProfileRepository.findById(workerId).orElse(null);
        if (profile != null) {
            profile.setLastLat(request.getLat());
            profile.setLastLng(request.getLng());
            workerProfileRepository.save(profile);
        }

        LocationLog log = LocationLog.builder()
                .id("loc-" + UUID.randomUUID().toString())
                .workerId(workerId)
                .lat(request.getLat())
                .lng(request.getLng())
                .heading(request.getHeading())
                .speed(request.getSpeed())
                .batteryLevel(request.getBatteryLevel())
                .build();
        locationLogRepository.save(log);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Location heartbeat recorded");
        return response;
    }
}
