package com.salaryneeds.service;

import com.salaryneeds.dto.LocationHeartbeatRequest;
import com.salaryneeds.dto.LocationPingRequest;
import com.salaryneeds.entity.LocationLog;
import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.entity.WorkerWallet;
import com.salaryneeds.exception.ApiException;
import com.salaryneeds.repository.LocationLogRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import com.salaryneeds.repository.WorkerWalletRepository;
import com.salaryneeds.util.UuidUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DutyLocationServiceImpl implements DutyLocationService {

    private final WorkerProfileRepository workerProfileRepository;
    private final WorkerWalletRepository workerWalletRepository;
    private final LocationLogRepository locationLogRepository;

    @Override
    @Transactional
    public Map<String, Object> toggleDuty(String workerId, Boolean requestedDuty) {
        UUID uuid = UuidUtil.parseUuid(workerId);
        if (uuid == null) {
            uuid = UUID.fromString("7f9a12c4-b8e1-4c2d-9a63-3e1b7f9a12c4");
        }
        final UUID finalUuid = uuid;
        WorkerProfile profile = workerProfileRepository.findById(finalUuid).orElseGet(() ->
                WorkerProfile.builder()
                        .id(finalUuid)
                        .name("Worker " + finalUuid.toString().substring(0, 8))
                        .phone("98765" + Math.abs(finalUuid.hashCode()) % 100000)
                        .email("worker" + finalUuid.toString().substring(0, 8) + "@salaryneeds.com")
                        .passwordHash("$2a$10$defaultPasswordHashSample1234567890abcdef")
                        .service("Technician")
                        .dutyOnline(false)
                        .build()
        );

        boolean targetDuty = requestedDuty != null ? requestedDuty : !Boolean.TRUE.equals(profile.getDutyOnline());

        if (targetDuty) {
            // Enforcement: Prepaid wallet balance >= ₹100
            WorkerWallet wallet = workerWalletRepository.findByWorkerId(profile.getId().toString()).orElse(null);
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

    @Override
    @Transactional
    public Map<String, Object> recordHeartbeat(String workerId, LocationHeartbeatRequest request) {
        UUID uuid = UuidUtil.parseUuid(workerId);
        if (uuid != null) {
            WorkerProfile profile = workerProfileRepository.findById(uuid).orElse(null);
            if (profile != null) {
                profile.setLastLat(request.getLat());
                profile.setLastLng(request.getLng());
                workerProfileRepository.save(profile);
            }
        }

        LocationLog log = LocationLog.builder()
                .id("loc-" + UUID.randomUUID().toString())
                .workerId(workerId != null ? workerId : (uuid != null ? uuid.toString() : "default"))
                .lat(request.getLat())
                .lng(request.getLng())
                .heading(request.getHeading())
                .speed(request.getSpeedKmh())
                .batteryLevel(request.getBatteryPct())
                .build();
        locationLogRepository.save(log);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", "ACK");
        response.put("recorded_at", Instant.now().toString());
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> recordPing(String workerId, LocationPingRequest request) {
        LocationHeartbeatRequest heartbeat = new LocationHeartbeatRequest();
        heartbeat.setLat(request.getLat());
        heartbeat.setLng(request.getLng());
        return recordHeartbeat(workerId, heartbeat);
    }
}
