package com.salaryneeds.service.admin;

import com.salaryneeds.dto.admin.AdminLoginRequestDTO;
import com.salaryneeds.dto.admin.AdminLoginResponseDTO;

import java.util.UUID;

public interface AdminAuthService {

    /**
     * Authenticates the admin by email + password.
     * Returns a signed JWT with role=ADMIN in the payload.
     */
    AdminLoginResponseDTO login(AdminLoginRequestDTO request);

    /**
     * Validates the raw JWT string (from Authorization: Bearer header).
     * Checks:
     *   1. Signature is valid
     *   2. Token is not expired
     *   3. role claim == "ADMIN"
     *
     * @return the adminId (UUID) embedded as the JWT subject
     * @throws com.salaryneeds.exception.UnauthorizedAdminException on any failure
     */
    UUID validateToken(String rawJwt);
}
