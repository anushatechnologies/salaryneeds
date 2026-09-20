package com.salaryneeds.service;

import com.salaryneeds.dto.LoginResponse;
import com.salaryneeds.dto.SendOtpRequest;
import com.salaryneeds.dto.WorkerLoginRequest;
import com.salaryneeds.dto.WorkerProfileDTO;
import com.salaryneeds.dto.WorkerRegisterRequest;
import com.salaryneeds.entity.Category;
import com.salaryneeds.entity.VerificationCode;
import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.entity.WorkerWallet;
import com.salaryneeds.entity.enums.AccountStatus;
import com.salaryneeds.exception.InvalidOtpException;
import com.salaryneeds.exception.PhoneAlreadyExistsException;
import com.salaryneeds.repository.CategoryRepository;
import com.salaryneeds.repository.VerificationCodeRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import com.salaryneeds.repository.WorkerWalletRepository;
import com.salaryneeds.util.UuidUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final WorkerProfileRepository workerProfileRepository;
    private final WorkerWalletRepository workerWalletRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final CategoryRepository categoryRepository;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public WorkerProfileDTO register(WorkerRegisterRequest request) {
        String phone = request.getPhone().trim();

        if (workerProfileRepository.existsByPhone(phone)) {
            throw new PhoneAlreadyExistsException("Worker with phone " + phone + " is already registered.");
        }

        UUID workerId = UUID.randomUUID();

        Category category = null;
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            UUID catUuid = UuidUtil.parseUuid(request.getCategoryId());
            if (catUuid != null) {
                category = categoryRepository.findById(catUuid).orElse(null);
            }
        }
        if (category == null) {
            category = categoryRepository.findAll().stream().findFirst().orElse(null);
        }

        WorkerProfile profile = WorkerProfile.builder()
                .id(workerId)
                .name(request.getName().trim())
                .phone(phone)
                .email(request.getEmail() != null ? request.getEmail().trim() : phone + "@salaryneeds.local")
                .passwordHash("otp-authenticated")
                .category(category)
                .service(request.getTrade())
                .skills(request.getSkills() != null ? String.join(", ", request.getSkills()) : "")
                .experienceYears(request.getExperienceYears() != null ? request.getExperienceYears() : 0)
                .pincode(request.getPincode())
                .city(request.getCity())
                .address(request.getAddress())
                .serviceAreasCsv(request.getServiceAreas() != null ? String.join(", ", request.getServiceAreas()) : "")
                .verified(false)
                .dutyOnline(false)
                .accountStatus(AccountStatus.PENDING_APPROVAL)
                .ratingAvg(BigDecimal.valueOf(5.00))
                .completedJobsCount(0)
                .totalReviews(0)
                .acceptanceRate(100.0)
                .completionRate(100.0)
                .tier("STANDARD")
                .build();

        WorkerProfile savedProfile = workerProfileRepository.save(profile);

        // Initialize empty wallet
        WorkerWallet wallet = WorkerWallet.builder()
                .id("wal-" + UUID.randomUUID().toString())
                .workerId(workerId.toString())
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

        return WorkerProfileDTO.fromEntity(savedProfile);
    }

    @Transactional
    public Map<String, Object> sendOtp(SendOtpRequest request) {
        String phone = request.getPhone().trim();

        // Generate 4-digit OTP
        int code = 1000 + random.nextInt(9000);
        String otpStr = String.valueOf(code);

        // Store verification code in DB
        VerificationCode vc = VerificationCode.builder()
                .phone(phone)
                .otp(otpStr)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .consumed(false)
                .build();
        verificationCodeRepository.save(vc);

        Map<String, Object> data = new HashMap<>();
        data.put("phone", phone);
        data.put("expires_in_seconds", 300);
        data.put("otp", otpStr); // Returned in response per spec
        return data;
    }

    @Transactional
    public LoginResponse login(WorkerLoginRequest request) {
        String phone = request.getEffectivePhone();
        String otp = request.getOtp() != null ? request.getOtp().trim() : "";

        // Verify OTP against verification_codes table or fallback test code "4829"
        Optional<VerificationCode> codeOpt = verificationCodeRepository.findTopByPhoneAndConsumedFalseOrderByCreatedAtDesc(phone);

        boolean isValid = false;
        if (codeOpt.isPresent()) {
            VerificationCode vc = codeOpt.get();
            if (vc.getExpiresAt().isAfter(LocalDateTime.now()) && vc.getOtp().equals(otp)) {
                isValid = true;
                vc.setConsumed(true);
                verificationCodeRepository.save(vc);
            }
        }

        // Test/Demo bypass OTP
        if (!isValid && ("4829".equals(otp) || "1234".equals(otp))) {
            isValid = true;
        }

        if (!isValid) {
            throw new InvalidOtpException("Invalid or expired 4-digit OTP code.");
        }

        // Find or create worker profile
        WorkerProfile worker = workerProfileRepository.findByPhone(phone).orElseGet(() -> {
            UUID newId = UUID.randomUUID();
            Category defaultCat = categoryRepository.findAll().stream().findFirst().orElse(null);
            WorkerProfile newWorker = WorkerProfile.builder()
                    .id(newId)
                    .name("Partner " + phone.substring(Math.max(0, phone.length() - 4)))
                    .phone(phone)
                    .email(phone + "@salaryneeds.local")
                    .passwordHash("otp-authenticated")
                    .category(defaultCat)
                    .service("Technician")
                    .skills("General Maintenance, Diagnostics")
                    .serviceAreasCsv("500072, 500081")
                    .verified(true)
                    .dutyOnline(true)
                    .accountStatus(AccountStatus.ACTIVE)
                    .ratingAvg(BigDecimal.valueOf(4.92))
                    .completedJobsCount(0)
                    .totalReviews(159)
                    .acceptanceRate(99.4)
                    .completionRate(99.8)
                    .tier("ELITE")
                    .build();

            WorkerWallet newWallet = WorkerWallet.builder()
                    .id("wal-" + UUID.randomUUID().toString())
                    .workerId(newId.toString())
                    .earningsBalance(new BigDecimal("1200.00"))
                    .todayEarnings(new BigDecimal("500.00"))
                    .thisWeekEarnings(new BigDecimal("2500.00"))
                    .thisMonthEarnings(new BigDecimal("12000.00"))
                    .lifetimeEarned(new BigDecimal("85000.00"))
                    .bankName("HDFC Bank")
                    .accountLast4("9142")
                    .ifsc("HDFC0001248")
                    .accountHolder(newWorker.getName())
                    .bankVerified(true)
                    .build();
            workerWalletRepository.save(newWallet);
            return workerProfileRepository.save(newWorker);
        });

        String token = worker.getId().toString();

        WorkerWallet wallet = workerWalletRepository.findByWorkerId(worker.getId().toString()).orElse(null);

        LoginResponse.WalletSummary walletSummary = LoginResponse.WalletSummary.builder()
                .dutyBalance(wallet != null ? wallet.getEarningsBalance() : BigDecimal.ZERO)
                .isActive(true)
                .build();

        LoginResponse.BankSummary bankSummary = LoginResponse.BankSummary.builder()
                .bankName(wallet != null && wallet.getBankName() != null ? wallet.getBankName() : "HDFC Bank")
                .accountLast4(wallet != null && wallet.getAccountLast4() != null ? wallet.getAccountLast4() : "9142")
                .verified(wallet != null && Boolean.TRUE.equals(wallet.getBankVerified()))
                .build();

        LoginResponse.WorkerLoginData loginData = LoginResponse.WorkerLoginData.builder()
                .id(worker.getId().toString())
                .name(worker.getName())
                .phone(worker.getPhone())
                .email(worker.getEmail())
                .trade(worker.getTrade())
                .categoryId(worker.getCategoryId() != null ? worker.getCategoryId().toString() : null)
                .categoryName(worker.getCategoryName())
                .skills(worker.getSkillsList())
                .experienceYears(worker.getExperienceYears())
                .pincode(worker.getPincode())
                .serviceAreas(worker.getServiceAreasList())
                .verified(worker.getVerified())
                .ratingAvg(worker.getRatingAvg() != null ? worker.getRatingAvg().doubleValue() : 5.0)
                .totalReviews(worker.getTotalReviews())
                .acceptanceRate(worker.getAcceptanceRate())
                .completionRate(worker.getCompletionRate())
                .tier(worker.getTier())
                .dutyOnline(worker.getDutyOnline())
                .walletSummary(walletSummary)
                .bankSummary(bankSummary)
                .build();

        return LoginResponse.builder()
                .success(true)
                .token(token)
                .worker(loginData)
                .build();
    }
}
