package com.salaryneeds.controller;

import com.salaryneeds.dto.WorkerLoginRequest;
import com.salaryneeds.dto.WorkerLoginResponse;
import com.salaryneeds.dto.WorkerSignupRequest;
import com.salaryneeds.dto.WorkerSignupResponse;
import com.salaryneeds.dto.CheckPhoneRequest;
import com.salaryneeds.dto.CheckPhoneResponse;
import com.salaryneeds.service.WorkerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/worker")
public class WorkerAuthController {

    @Autowired
    private WorkerService workerService;
    @PostMapping("/check-phone")
    public ResponseEntity<CheckPhoneResponse> checkPhone(@Valid @RequestBody CheckPhoneRequest request) {
        CheckPhoneResponse response = workerService.checkPhone(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<WorkerSignupResponse> signup(@Valid @RequestBody WorkerSignupRequest request) {
        WorkerSignupResponse response = workerService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<WorkerLoginResponse> login(@Valid @RequestBody WorkerLoginRequest request) {
        WorkerLoginResponse response = workerService.login(request);
        return ResponseEntity.ok(response);
    }
}
