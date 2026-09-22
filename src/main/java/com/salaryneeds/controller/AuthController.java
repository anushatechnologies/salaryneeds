package com.salaryneeds.controller;

import com.salaryneeds.dto.*;
import com.salaryneeds.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/worker/auth", "/v1/worker/auth"})
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register", consumes = {org.springframework.http.MediaType.APPLICATION_JSON_VALUE, org.springframework.http.MediaType.ALL_VALUE})
    public ResponseEntity<ApiResponse<WorkerProfileDTO>> register(@Valid @RequestBody WorkerRegisterRequest request) {
        WorkerProfileDTO profile = authService.register(request);
        return new ResponseEntity<>(ApiResponse.ok("Worker profile registered successfully.", profile), HttpStatus.CREATED);
    }

    @PostMapping(value = "/register", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<WorkerProfileDTO>> registerMultipart(
            @ModelAttribute WorkerRegisterRequest request,
            @RequestPart(value = "aadhar", required = false) org.springframework.web.multipart.MultipartFile aadhar,
            @RequestPart(value = "pan", required = false) org.springframework.web.multipart.MultipartFile pan,
            @RequestPart(value = "aadharFile", required = false) org.springframework.web.multipart.MultipartFile aadharFile,
            @RequestPart(value = "panFile", required = false) org.springframework.web.multipart.MultipartFile panFile) {
        if (aadhar != null && request.getAadharFile() == null) request.setAadharFile(aadhar);
        if (aadharFile != null) request.setAadharFile(aadharFile);
        if (pan != null && request.getPanFile() == null) request.setPanFile(pan);
        if (panFile != null) request.setPanFile(panFile);

        WorkerProfileDTO profile = authService.register(request);
        return new ResponseEntity<>(ApiResponse.ok("Worker profile registered successfully.", profile), HttpStatus.CREATED);
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        Map<String, Object> data = authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("4-digit verification code sent successfully via SMS.", data));
    }

    @PostMapping({"/login", "/verify-otp"})
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody WorkerLoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
