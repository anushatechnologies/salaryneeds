package com.salaryneeds.service;

import com.salaryneeds.dto.*;
import com.salaryneeds.entity.Category;
import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.repository.CategoryRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

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
            throw new RuntimeException("Phone number already in use");
        }

        if (!"1234".equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        Category category = categoryRepository.findById(request.getCategory_id())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        WorkerProfile worker = WorkerProfile.builder()
                .name(request.getName())
                // Use a dummy email/password since the schema requires it currently, 
                // or we can just leave it if we update the entity
                .email(request.getPhone() + "@dummy.com") 
                .passwordHash("dummy-hash")
                .phone(request.getPhone())
                .category(category)
                .service(request.getService())
                .skills(request.getSkills())
                .experienceYears(request.getExperience_years())
                .pincode(request.getPincode())
                .verified(false)
                .emailVerified(false)
                .phoneVerified(true)
                .accountStatus("ACTIVE")
                .build();

        worker = workerProfileRepository.save(worker);

        return WorkerSignupResponse.builder()
                .worker_id(worker.getId())
                .message("OTP sent, profile pending document verification")
                .build();
    }

    public WorkerLoginResponse login(WorkerLoginRequest request) {
        WorkerProfile worker = workerProfileRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        // Dummy OTP validation for now
        if (!"1234".equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        // Generate dummy JWT token for now
        String token = "dummy-jwt-token-for-" + worker.getId();

        return WorkerLoginResponse.builder()
                .token(token)
                .worker_id(worker.getId())
                .verified(worker.getVerified())
                .account_status(worker.getAccountStatus())
                .build();
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
                .account_status(worker.getAccountStatus())
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
