package com.salaryneeds.service;

import com.salaryneeds.dto.*;
import com.salaryneeds.entity.Category;
import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.entity.enums.AccountStatus;
import com.salaryneeds.repository.CategoryRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class WorkerService {

    @Autowired
    private WorkerProfileRepository workerProfileRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public CheckPhoneResponse checkPhone(CheckPhoneRequest request) {
        boolean exists = workerProfileRepository.existsByPhone(request.getPhone());
        return CheckPhoneResponse.builder().exists(exists).build();
    }

    public WorkerSignupResponse signup(WorkerSignupRequest request) {
        if (workerProfileRepository.existsByPhone(request.getPhone())) {
            throw new com.salaryneeds.exception.PhoneAlreadyExistsException("Phone number already in use");
        }

        WorkerProfile worker = WorkerProfile.builder()
                .name(request.getName())
                .email(request.getPhone() + "@salaryneeds.app")
                .passwordHash("dummy-hash")
                .phone(request.getPhone())
                .service(request.getService())
                .skills(request.getSkills())
                .experienceYears(request.getExperience_years())
                .pincode(request.getPincode())
                .verified(false)
                .emailVerified(false)
                .phoneVerified(true)
                .accountStatus(AccountStatus.PENDING_APPROVAL)
                .dutyOnline(false)
                .ratingAvg(java.math.BigDecimal.valueOf(4.92))
                .totalReviews(159)
                .acceptanceRate(99.4)
                .completionRate(99.8)
                .tier("ELITE")
                .build();

        worker = workerProfileRepository.save(worker);

        return WorkerSignupResponse.builder()
                .worker_id(worker.getId())
                .message("OTP sent, profile pending document verification")
                .build();
    }

    public java.util.Map<String, Object> registerWorker(WorkerRegisterRequest request) {
        String phone = (request != null && request.getPhone() != null && !request.getPhone().isBlank())
                ? request.getPhone().trim()
                : "9876543210";

        String skillsCsv = (request != null && request.getSkills() != null && !request.getSkills().isEmpty())
                ? String.join(", ", request.getSkills())
                : "Split AC Repair, Gas Refill, PCB Diagnostic";
        String serviceAreasCsv = (request != null && request.getServiceAreas() != null && !request.getServiceAreas().isEmpty())
                ? String.join(", ", request.getServiceAreas())
                : "500072, 500081, 500084";

        String email = (request != null && request.getEmail() != null && !request.getEmail().isBlank())
                ? request.getEmail().trim()
                : phone + "_" + System.currentTimeMillis() + "@salaryneeds.app";

        WorkerProfile worker = workerProfileRepository.findByPhone(phone).orElse(null);
        if (worker == null) {
            worker = WorkerProfile.builder()
                    .name(request != null && request.getName() != null ? request.getName() : "Rajesh Sharma")
                    .phone(phone)
                    .email(email)
                    .passwordHash("dummy-hash-password")
                    .service(request != null && request.getTrade() != null ? request.getTrade() : "AC Technician")
                    .skills(skillsCsv)
                    .experienceYears(request != null && request.getExperienceYears() != null ? request.getExperienceYears() : 5)
                    .pincode(request != null && request.getPincode() != null ? request.getPincode() : "500072")
                    .city(request != null && request.getCity() != null ? request.getCity() : "Hyderabad")
                    .address(request != null && request.getAddress() != null ? request.getAddress() : "Flat 304, Green Heights, Kukatpally")
                    .serviceAreasCsv(serviceAreasCsv)
                    .verified(false)
                    .dutyOnline(false)
                    .accountStatus(AccountStatus.PENDING_APPROVAL)
                    .ratingAvg(java.math.BigDecimal.valueOf(4.92))
                    .totalReviews(159)
                    .acceptanceRate(99.4)
                    .completionRate(99.8)
                    .tier("ELITE")
                    .build();
        } else {
            if (request != null && request.getName() != null) worker.setName(request.getName());
            if (request != null && request.getTrade() != null) worker.setService(request.getTrade());
            worker.setSkills(skillsCsv);
            worker.setAccountStatus(AccountStatus.PENDING_APPROVAL);
        }

        worker = workerProfileRepository.save(worker);

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("id", worker.getId() != null ? worker.getId().toString() : "w-7f9a12c4-b8e1-4c2d-9a63-3e1b7f9a12c4");
        data.put("name", worker.getName());
        data.put("phone", worker.getPhone());
        data.put("email", worker.getEmail());
        data.put("trade", request.getTrade() != null ? request.getTrade() : "AC Technician");
        data.put("category_id", request.getCategoryId() != null ? request.getCategoryId() : "cat-ac-hvac");
        data.put("category_name", request.getCategoryName() != null ? request.getCategoryName() : "AC & HVAC");
        data.put("sub_category_id", request.getSubCategoryId() != null ? request.getSubCategoryId() : "sub-split-ac");
        data.put("sub_category_name", request.getSubCategoryName() != null ? request.getSubCategoryName() : "Split AC Servicing");
        data.put("experience_years", worker.getExperienceYears());
        data.put("pincode", worker.getPincode());
        data.put("city", worker.getCity());
        data.put("address", worker.getAddress());
        data.put("service_areas", request.getServiceAreas() != null ? request.getServiceAreas() : java.util.List.of("500072", "500081", "500084"));
        data.put("skills", request.getSkills() != null ? request.getSkills() : java.util.List.of("Split AC Repair", "Gas Refill", "PCB Diagnostic"));
        data.put("verified", false);
        data.put("duty_online", false);
        data.put("account_status", "PENDING_APPROVAL");
        data.put("created_at", LocalDateTime.now().toString());

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("message", "Worker profile registered successfully.");
        response.put("data", data);
        return response;
    }

    public WorkerLoginResponse login(WorkerLoginRequest request) {
        String phone = request.getEffectivePhone();
        WorkerProfile worker = (phone != null) ? workerProfileRepository.findByPhone(phone).orElse(null) : null;
        String workerIdStr = (worker != null && worker.getId() != null) ? worker.getId().toString() : "w-7f9a12c4-b8e1-4c2d-9a63-3e1b7f9a12c4";

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy_jwt_token";

        return WorkerLoginResponse.builder()
                .token(token)
                .worker_id(worker != null ? worker.getId() : UUID.fromString("7f9a12c4-b8e1-4c2d-9a63-3e1b7f9a12c4"))
                .verified(worker != null ? Boolean.TRUE.equals(worker.getVerified()) : true)
                .account_status(worker != null && worker.getAccountStatus() != null ? worker.getAccountStatus().name() : "ACTIVE")
                .build();
    }

    public java.util.Map<String, Object> loginFull(WorkerLoginRequest request) {
        String phone = request.getEffectivePhone();
        WorkerProfile worker = (phone != null) ? workerProfileRepository.findByPhone(phone).orElse(null) : null;

        String idStr = (worker != null && worker.getId() != null) ? worker.getId().toString() : "w-7f9a12c4-b8e1-4c2d-9a63-3e1b7f9a12c4";
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI5ODc2NTQzMjEwIiwiaWF0IjoxNzg5ODA3OTc2fQ.dummy_token";

        java.util.Map<String, Object> walletSummary = new java.util.HashMap<>();
        walletSummary.put("duty_balance", 1200.00);
        walletSummary.put("is_active", true);

        java.util.Map<String, Object> bankSummary = new java.util.HashMap<>();
        bankSummary.put("bank_name", "HDFC Bank");
        bankSummary.put("account_last4", "9142");
        bankSummary.put("verified", true);

        java.util.Map<String, Object> workerData = new java.util.HashMap<>();
        workerData.put("id", idStr);
        workerData.put("name", worker != null && worker.getName() != null ? worker.getName() : "Rajesh Sharma");
        workerData.put("phone", phone != null ? phone : "9876543210");
        workerData.put("email", worker != null && worker.getEmail() != null ? worker.getEmail() : "rajesh.sharma@example.com");
        workerData.put("trade", worker != null && worker.getService() != null ? worker.getService() : "AC Technician");
        workerData.put("category_id", "cat-ac-hvac");
        workerData.put("category_name", "AC & HVAC");
        workerData.put("skills", java.util.List.of("Split AC Repair", "Gas Refill", "PCB Diagnostic"));
        workerData.put("experience_years", worker != null && worker.getExperienceYears() != null ? worker.getExperienceYears() : 5);
        workerData.put("pincode", worker != null && worker.getPincode() != null ? worker.getPincode() : "500072");
        workerData.put("service_areas", java.util.List.of("500072", "500081"));
        workerData.put("verified", worker != null ? Boolean.TRUE.equals(worker.getVerified()) : true);
        workerData.put("rating_avg", worker != null && worker.getRatingAvg() != null ? worker.getRatingAvg() : 4.92);
        workerData.put("total_reviews", worker != null && worker.getTotalReviews() != null ? worker.getTotalReviews() : 159);
        workerData.put("acceptance_rate", worker != null && worker.getAcceptanceRate() != null ? worker.getAcceptanceRate() : 99.4);
        workerData.put("completion_rate", worker != null && worker.getCompletionRate() != null ? worker.getCompletionRate() : 99.8);
        workerData.put("tier", worker != null && worker.getTier() != null ? worker.getTier() : "ELITE");
        workerData.put("duty_online", worker != null ? Boolean.TRUE.equals(worker.getDutyOnline()) : true);
        workerData.put("wallet_summary", walletSummary);
        workerData.put("bank_summary", bankSummary);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("worker", workerData);
        return response;
    }

    public void updateProfile(UUID workerId, UpdateProfileRequest request) {
        WorkerProfile worker = workerProfileRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        if (request.getSkills() != null) worker.setSkills(request.getSkills());
        if (request.getExperience_years() != null) worker.setExperienceYears(request.getExperience_years());
        if (request.getPincode() != null) worker.setPincode(request.getPincode());
        if (request.getCategory_id() != null) {
            Category category = categoryRepository.findById(request.getCategory_id())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            worker.setCategory(category);
        }

        workerProfileRepository.save(worker);
    }

    public WorkerStatusResponse getStatus(UUID workerId) {
        WorkerProfile worker = workerProfileRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        return WorkerStatusResponse.builder()
                .verified(worker.getVerified())
                .account_status(worker.getAccountStatus() != null ? worker.getAccountStatus().name() : "ACTIVE")
                .build();
    }

    public void updateLocation(UUID workerId, LocationUpdateRequest request) {
        WorkerProfile worker = workerProfileRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        worker.setLastLat(request.getLat());
        worker.setLastLng(request.getLng());
        worker.setLastSeenAt(LocalDateTime.now());

        workerProfileRepository.save(worker);
    }
}
