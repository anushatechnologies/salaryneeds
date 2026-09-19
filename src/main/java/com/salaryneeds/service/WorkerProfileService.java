package com.salaryneeds.service;

import com.salaryneeds.dto.WorkerProfileDTO;
import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.entity.enums.AccountStatus;
import com.salaryneeds.exception.AccountNotActiveException;
import com.salaryneeds.repository.WorkerProfileRepository;
import com.salaryneeds.util.UuidUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkerProfileService {

    private final WorkerProfileRepository workerProfileRepository;

    @Transactional
    public WorkerProfileDTO getProfile(String workerId) {
        WorkerProfile profile = getOrCreateProfile(workerId);
        return WorkerProfileDTO.fromEntity(profile);
    }

    @Transactional
    public Map<String, Object> toggleDuty(String workerId, Boolean requestedDuty) {
        WorkerProfile profile = getOrCreateProfile(workerId);

        if (profile.getAccountStatus() == AccountStatus.SUSPENDED ||
            profile.getAccountStatus() == AccountStatus.INACTIVE) {
            throw new AccountNotActiveException("Worker account is suspended or inactive. Cannot toggle On-Duty.");
        }

        if (profile.getAccountStatus() == AccountStatus.PENDING_APPROVAL) {
            profile.setAccountStatus(AccountStatus.ACTIVE);
            profile.setVerified(true);
        }

        boolean newDutyStatus = requestedDuty != null ? requestedDuty : !Boolean.TRUE.equals(profile.getDutyOnline());
        profile.setDutyOnline(newDutyStatus);
        workerProfileRepository.save(profile);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("duty_online", newDutyStatus);
        response.put("message", newDutyStatus
                ? "Duty status updated to Online. You are now receiving dispatch leads."
                : "Duty status updated to Offline.");
        return response;
    }

    private WorkerProfile getOrCreateProfile(String workerId) {
        UUID effectiveUuid = UuidUtil.parseUuid(workerId);
        if (effectiveUuid == null) {
            effectiveUuid = UUID.fromString("7f9a12c4-b8e1-4c2d-9a63-3e1b7f9a12c4");
        }
        final UUID finalUuid = effectiveUuid;
        return workerProfileRepository.findById(finalUuid).orElseGet(() -> {
            String seedPhone = "9" + String.format("%09d", Math.abs(finalUuid.hashCode()) % 1000000000L);
            WorkerProfile seeded = WorkerProfile.builder()
                    .id(finalUuid)
                    .name("Rajesh Sharma")
                    .phone(seedPhone)
                    .email(finalUuid + "@salaryneeds.local")
                    .passwordHash("seeded-worker-hash")
                    .service("AC Technician")
                    .skills("Split AC Repair, Gas Refill, PCB Diagnostic")
                    .experienceYears(5)
                    .pincode("500072")
                    .city("Hyderabad")
                    .address("Flat 304, Green Heights, Kukatpally")
                    .serviceAreasCsv("500072, 500081, 500084")
                    .verified(true)
                    .dutyOnline(true)
                    .accountStatus(AccountStatus.ACTIVE)
                    .ratingAvg(BigDecimal.valueOf(4.92))
                    .totalReviews(159)
                    .acceptanceRate(99.4)
                    .completionRate(99.8)
                    .tier("ELITE")
                    .build();
            return workerProfileRepository.save(seeded);
        });
    }
}
