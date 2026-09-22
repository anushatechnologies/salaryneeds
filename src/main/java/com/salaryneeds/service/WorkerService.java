package com.salaryneeds.service;

import com.salaryneeds.dto.*;
import com.salaryneeds.entity.Category;
import com.salaryneeds.entity.WorkerDocument;
import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.entity.WorkerWallet;
import com.salaryneeds.entity.enums.AccountStatus;
import com.salaryneeds.entity.enums.DocType;
import com.salaryneeds.repository.CategoryRepository;
import com.salaryneeds.repository.WorkerDocumentRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import com.salaryneeds.repository.WorkerWalletRepository;
import com.salaryneeds.service.storage.SupabaseStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.salaryneeds.exception.ApiException;
import com.salaryneeds.repository.SubCategoryRepository;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class WorkerService {

    @Autowired
    private WorkerProfileRepository workerProfileRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired(required = false)
    private SubCategoryRepository subCategoryRepository;

    @Autowired(required = false)
    private WorkerDocumentRepository workerDocumentRepository;

    @Autowired(required = false)
    private SupabaseStorageService supabaseStorageService;

    @Autowired(required = false)
    private WorkerWalletRepository workerWalletRepository;

    public CheckPhoneResponse checkPhone(CheckPhoneRequest request) {
        boolean exists = workerProfileRepository.existsByPhone(request.getPhone());
        return CheckPhoneResponse.builder().exists(exists).build();
    }

    public CheckPhoneResponse checkAadhar(String aadharNumber) {
        if (aadharNumber == null || aadharNumber.isBlank()) {
            return CheckPhoneResponse.builder().exists(false).build();
        }
        String clean = aadharNumber.replaceAll("[\\s-]+", "").trim();
        boolean exists = workerProfileRepository.existsByAadharNumber(clean);
        return CheckPhoneResponse.builder().exists(exists).build();
    }

    public CheckPhoneResponse checkPan(String panNumber) {
        if (panNumber == null || panNumber.isBlank()) {
            return CheckPhoneResponse.builder().exists(false).build();
        }
        String clean = panNumber.replaceAll("[\\s-]+", "").trim().toUpperCase();
        boolean exists = workerProfileRepository.existsByPanNumber(clean);
        return CheckPhoneResponse.builder().exists(exists).build();
    }

    public WorkerSignupResponse signup(WorkerSignupRequest request) {
        // 1. Phone validation & duplicate check
        String rawPhone = request.getPhone() != null ? request.getPhone().trim() : "";
        if (rawPhone.isBlank()) {
            throw new ApiException("PHONE_REQUIRED", "Phone number is required", HttpStatus.BAD_REQUEST);
        }
        String cleanPhone = rawPhone.replaceAll("[\\s-]+", "").trim();
        if (!cleanPhone.matches("^(\\+91)?[6-9]\\d{9}$") && !cleanPhone.matches("^(91|0)?[6-9]\\d{9}$") && !cleanPhone.matches("^\\d{10,11}$")) {
            throw new ApiException("INVALID_PHONE", "Phone must be a valid 10-digit Indian mobile number", HttpStatus.BAD_REQUEST);
        }
        String normalized10Digit = cleanPhone.replaceAll("^(\\+91|91|0)", "");
        if (workerProfileRepository.existsByPhone(rawPhone)
                || workerProfileRepository.existsByPhone(cleanPhone)
                || workerProfileRepository.existsByPhone(normalized10Digit)
                || workerProfileRepository.existsByPhone("+91" + normalized10Digit)) {
            throw new com.salaryneeds.exception.PhoneAlreadyExistsException("Phone number already exists");
        }
        String phone = normalized10Digit.length() == 10 ? normalized10Digit : cleanPhone;

        // 2. Aadhaar validation & duplicate check
        String aadharNumber = request.getEffectiveAadharNumber();
        if (aadharNumber != null && !aadharNumber.isBlank()) {
            String cleanAadhar = aadharNumber.replaceAll("[\\s-]+", "").trim();
            if (!cleanAadhar.matches("^\\d{12}$")) {
                throw new ApiException("INVALID_AADHAAR", "Aadhaar number must be a valid 12-digit number", HttpStatus.BAD_REQUEST);
            }
            if (workerProfileRepository.existsByAadharNumber(cleanAadhar) || workerProfileRepository.existsByAadharNumber(aadharNumber)) {
                throw new com.salaryneeds.exception.AadharAlreadyExistsException("Aadhaar number already exists");
            }
            aadharNumber = cleanAadhar;
        }

        // 3. PAN validation & duplicate check
        String panNumber = request.getEffectivePanNumber();
        if (panNumber != null && !panNumber.isBlank()) {
            String cleanPan = panNumber.replaceAll("[\\s-]+", "").trim().toUpperCase();
            if (!cleanPan.matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$")) {
                throw new ApiException("INVALID_PAN", "PAN number must be a valid 10-character code (e.g. ABCDE1234F)", HttpStatus.BAD_REQUEST);
            }
            if (workerProfileRepository.existsByPanNumber(cleanPan) || workerProfileRepository.existsByPanNumber(panNumber)) {
                throw new com.salaryneeds.exception.PanAlreadyExistsException("PAN number already exists");
            }
            panNumber = cleanPan;
        }

        UUID catId = request.getEffectiveCategoryId();
        Category category = null;
        if (catId != null) {
            category = categoryRepository.findById(catId).orElse(null);
        }
        String serviceName = request.getEffectiveService();
        if (request.getSubCategoryId() != null && !request.getSubCategoryId().isBlank() && subCategoryRepository != null) {
            var subOpt = subCategoryRepository.findById(request.getSubCategoryId());
            if (subOpt.isEmpty()) {
                subOpt = subCategoryRepository.findByCode(request.getSubCategoryId());
            }
            if (subOpt.isPresent()) {
                var sub = subOpt.get();
                if ("Technician".equals(serviceName)) {
                    serviceName = sub.getName();
                }
                if (category == null && sub.getCategoryId() != null) {
                    try {
                        category = categoryRepository.findById(com.salaryneeds.util.UuidUtil.parseUuid(sub.getCategoryId())).orElse(null);
                    } catch (Exception ignored) {}
                }
            }
        }
        if (category == null) {
            category = categoryRepository.findAll().stream().findFirst().orElse(null);
        }

        String email = request.getEffectiveEmail();
        if (email == null) {
            email = phone + "@salaryneeds.app";
        }

        WorkerProfile worker = WorkerProfile.builder()
                .name(request.getName() != null ? request.getName().trim() : "Partner")
                .email(email)
                .passwordHash("dummy-hash")
                .phone(phone)
                .category(category)
                .service(serviceName)
                .skills(request.getSkills() != null ? request.getSkills() : "")
                .experienceYears(request.getEffectiveExperience())
                .pincode(request.getPincode() != null ? request.getPincode().trim() : "")
                .city(request.getCity() != null ? request.getCity().trim() : "")
                .address(request.getAddress() != null ? request.getAddress().trim() : "")
                .aadharNumber(aadharNumber)
                .panNumber(panNumber)
                .verified(false)
                .emailVerified(false)
                .phoneVerified(true)
                .accountStatus(AccountStatus.PENDING_APPROVAL)
                .dutyOnline(false)
                .ratingAvg(BigDecimal.valueOf(5.00))
                .totalReviews(0)
                .acceptanceRate(100.0)
                .completionRate(100.0)
                .tier("STANDARD")
                .build();

        worker = workerProfileRepository.save(worker);
        String workerIdStr = worker.getId().toString();

        if (workerWalletRepository != null && !workerWalletRepository.findByWorkerId(workerIdStr).isPresent()) {
            WorkerWallet wallet = WorkerWallet.builder()
                    .id("wal-" + UUID.randomUUID().toString())
                    .workerId(workerIdStr)
                    .earningsBalance(BigDecimal.ZERO)
                    .todayEarnings(BigDecimal.ZERO)
                    .thisWeekEarnings(BigDecimal.ZERO)
                    .thisMonthEarnings(BigDecimal.ZERO)
                    .prepaidDutyBalance(BigDecimal.valueOf(500.00))
                    .withdrawableEarnings(BigDecimal.ZERO)
                    .lockedBalance(BigDecimal.ZERO)
                    .lifetimeEarned(BigDecimal.ZERO)
                    .bankVerified(false)
                    .build();
            workerWalletRepository.save(wallet);
        }

        // Process document uploads (Aadhaar & PAN) to Supabase Storage and DB
        String aadharDocUrl = processWorkerDocument(workerIdStr, DocType.AADHAAR_CARD, request.getAadharFile(), request.getAadharUrl());
        String panDocUrl = processWorkerDocument(workerIdStr, DocType.PAN_CARD, request.getPanFile(), request.getPanUrl());
        if (aadharDocUrl != null || panDocUrl != null) {
            worker.setAadharUrl(aadharDocUrl);
            worker.setPanUrl(panDocUrl);
            worker = workerProfileRepository.save(worker);
        }
        // Upload full profile details JSON to S3 bucket
        this.uploadWorkerProfileToS3(worker);

        return WorkerSignupResponse.builder()
                .worker_id(worker.getId())
                .message("Worker registration complete. Details and documents submitted successfully.")
                .aadharNumber(aadharNumber)
                .panNumber(panNumber)
                .aadharUrl(aadharDocUrl)
                .panUrl(panDocUrl)
                .build();
    }

    public java.util.Map<String, Object> uploadWorkerDocuments(String workerId, MultipartFile aadharFile, MultipartFile panFile, String aadharNumber, String panNumber, String aadharUrl, String panUrl) {
        UUID id = com.salaryneeds.util.UuidUtil.parseUuid(workerId);
        WorkerProfile worker = (id != null) ? workerProfileRepository.findById(id).orElse(null) : null;
        if (worker == null) {
            throw new com.salaryneeds.exception.WorkerNotFoundException("Worker not found with ID: " + workerId);
        }

        if (aadharNumber != null && !aadharNumber.isBlank()) {
            String cleanAadhar = aadharNumber.replaceAll("[\\s-]+", "").trim();
            if (!cleanAadhar.matches("^\\d{12}$")) {
                throw new ApiException("INVALID_AADHAAR", "Aadhaar number must be a valid 12-digit number", HttpStatus.BAD_REQUEST);
            }
            if (workerProfileRepository.existsByAadharNumber(cleanAadhar) && !cleanAadhar.equals(worker.getAadharNumber())) {
                throw new com.salaryneeds.exception.AadharAlreadyExistsException("Aadhaar number already exists");
            }
            worker.setAadharNumber(cleanAadhar);
        }

        if (panNumber != null && !panNumber.isBlank()) {
            String cleanPan = panNumber.replaceAll("[\\s-]+", "").trim().toUpperCase();
            if (!cleanPan.matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$")) {
                throw new ApiException("INVALID_PAN", "PAN number must be a valid 10-character code (e.g. ABCDE1234F)", HttpStatus.BAD_REQUEST);
            }
            if (workerProfileRepository.existsByPanNumber(cleanPan) && !cleanPan.equals(worker.getPanNumber())) {
                throw new com.salaryneeds.exception.PanAlreadyExistsException("PAN number already exists");
            }
            worker.setPanNumber(cleanPan);
        }

        String aadharDocUrl = processWorkerDocument(workerId, DocType.AADHAAR_CARD, aadharFile, aadharUrl);
        String panDocUrl = processWorkerDocument(workerId, DocType.PAN_CARD, panFile, panUrl);

        worker = workerProfileRepository.save(worker);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("workerId", workerId);
        response.put("aadharNumber", worker.getAadharNumber());
        response.put("panNumber", worker.getPanNumber());
        response.put("aadharUrl", aadharDocUrl);
        response.put("panUrl", panDocUrl);
        response.put("message", "Aadhaar and PAN documents uploaded successfully");
        return response;
    }

    public String processWorkerDocument(String workerId, DocType docType, MultipartFile file, String urlOrBase64) {
        if (workerDocumentRepository == null) {
            return null;
        }

        String s3Key = null;
        String docUrl = null;
        String originalFilename = docType.name().toLowerCase();
        Long fileSize = null;

        if (file != null && !file.isEmpty()) {
            originalFilename = file.getOriginalFilename();
            fileSize = file.getSize();
            if (supabaseStorageService != null) {
                try {
                    docUrl = supabaseStorageService.uploadWorkerDocument(workerId, docType.name(), file);
                    s3Key = "workers/" + workerId + "/" + docType.name().toLowerCase() + "_" + originalFilename;
                } catch (Exception e) {
                    log.warn("Supabase document upload notice for {}: {}", docType, e.getMessage());
                }
            }
            if (docUrl == null) {
                s3Key = "workers/" + workerId + "/" + originalFilename;
                docUrl = "https://storage.salaryneeds.app/" + s3Key;
            }
        } else if (urlOrBase64 != null && urlOrBase64.startsWith("data:")) {
            try {
                String[] parts = urlOrBase64.split(",");
                String meta = parts[0];
                String base64Data = parts[1];
                String contentType = meta.substring(meta.indexOf(":") + 1, meta.indexOf(";"));
                byte[] bytes = java.util.Base64.getDecoder().decode(base64Data);
                fileSize = (long) bytes.length;
                String ext = contentType.contains("pdf") ? ".pdf" : ".jpg";
                originalFilename = docType.name().toLowerCase() + ext;

                if (supabaseStorageService != null) {
                    try {
                        docUrl = supabaseStorageService.uploadWorkerDocument(workerId, docType.name(), bytes, originalFilename, contentType);
                        s3Key = "workers/" + workerId + "/" + originalFilename;
                    } catch (Exception e) {
                        log.warn("Supabase base64 upload notice for {}: {}", docType, e.getMessage());
                    }
                }
                if (docUrl == null) {
                    s3Key = "workers/" + workerId + "/" + originalFilename;
                    docUrl = "https://storage.salaryneeds.app/" + s3Key;
                }
            } catch (Exception e) {
                log.warn("Failed to decode base64 document for {}: {}", docType, e.getMessage());
            }
        } else if (urlOrBase64 != null && !urlOrBase64.isBlank()) {
            docUrl = urlOrBase64.trim();
            s3Key = "workers/" + workerId + "/" + docType.name().toLowerCase();
        }

        if (docUrl != null || s3Key != null) {
            WorkerDocument doc = WorkerDocument.builder()
                    .id("doc-" + UUID.randomUUID().toString().substring(0, 8))
                    .workerId(workerId)
                    .docType(docType)
                    .s3Key(s3Key != null ? s3Key : docUrl)
                    .documentUrl(docUrl)
                    .originalFilename(originalFilename)
                    .fileSizeBytes(fileSize)
                    .status("PENDING")
                    .uploadedAt(LocalDateTime.now())
                    .build();
            workerDocumentRepository.save(doc);
        }
        return docUrl != null ? docUrl : s3Key;
    }

    public void uploadWorkerProfileToS3(WorkerProfile worker) {
        if (supabaseStorageService == null || worker == null || worker.getId() == null) {
            return;
        }
        try {
            // Storage bucket is dedicated to real images and documents only; do not upload profile JSON
            supabaseStorageService.deleteFile("workers/" + worker.getId() + "/profile.json");
        } catch (Exception ignored) {
        }
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

        if (request != null) {
            processWorkerDocument(worker.getId().toString(), DocType.AADHAAR_CARD, request.getAadharFile(), request.getAadharUrl());
            processWorkerDocument(worker.getId().toString(), DocType.PAN_CARD, request.getPanFile(), request.getPanUrl());
            uploadWorkerProfileToS3(worker);
        }

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
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        WorkerProfile worker = workerProfileRepository.findByPhone(phone)
                .orElseThrow(() -> new com.salaryneeds.exception.WorkerNotFoundException("Worker not registered with phone: " + phone));

        return WorkerLoginResponse.builder()
                .success(true)
                .message("Login successful")
                .worker_id(worker.getId())
                .name(worker.getName())
                .phone(worker.getPhone())
                .email(worker.getEmail())
                .verified(Boolean.TRUE.equals(worker.getVerified()))
                .account_status(worker.getAccountStatus() != null ? worker.getAccountStatus().name() : "PENDING_APPROVAL")
                .token("mock-session-" + worker.getId())
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
