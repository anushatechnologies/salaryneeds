package com.salaryneeds.controller.admin;

import com.salaryneeds.dto.admin.AdminLoginRequestDTO;
import com.salaryneeds.dto.admin.AdminLoginResponseDTO;
import com.salaryneeds.service.admin.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/admin/auth", "/auth", "/api/auth"})
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * Supports POST /api/admin/auth/login and POST /auth/login
     * Authenticates email + password, returns JWT token with role=ADMIN
     */
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponseDTO> login(
            @Valid @RequestBody AdminLoginRequestDTO request) {
        return ResponseEntity.ok(adminAuthService.login(request));
    }
}
