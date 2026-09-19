package com.salaryneeds.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Returned by POST /api/admin/auth/login.
 *
 * The frontend should:
 *   1. Store the JWT token (localStorage / sessionStorage)
 *   2. Verify role == "ADMIN"
 *   3. Navigate to /admin/dashboard
 */
@Getter
@Builder
public class AdminLoginResponseDTO {

    private UUID   adminId;
    private String name;
    private String email;
    /**
     * Signed JWT — embed in every subsequent request as:
     *   Authorization: Bearer <token>
     */
    private String token;
    /** Always "ADMIN" — frontend uses this to confirm the role before redirecting */
    private String role;
    /** Token validity in seconds (default 86400 = 24 hours) */
    private long   expiresIn;
}
