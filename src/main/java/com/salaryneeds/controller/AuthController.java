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

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<WorkerProfileDTO>> register(@Valid @RequestBody WorkerRegisterRequest request) {
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
