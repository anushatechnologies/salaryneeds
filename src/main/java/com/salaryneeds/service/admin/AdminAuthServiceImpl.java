package com.salaryneeds.service.admin;

import com.salaryneeds.config.JwtUtil;
import com.salaryneeds.dto.admin.AdminLoginRequestDTO;
import com.salaryneeds.dto.admin.AdminLoginResponseDTO;
import com.salaryneeds.entity.Admin;
import com.salaryneeds.exception.AdminNotFoundException;
import com.salaryneeds.exception.UnauthorizedAdminException;
import com.salaryneeds.repository.AdminRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;

    @Value("${admin.jwt.expiration-ms:86400000}")
    private long expirationMs;

    // ── Login ─────────────────────────────────────────────────────────────

    @Override
    public AdminLoginResponseDTO login(AdminLoginRequestDTO request) {

        // 1. Lookup admin by email
        Admin admin = adminRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AdminNotFoundException("Invalid email or password"));

        // 2. Verify password
        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new AdminNotFoundException("Invalid email or password");
        }

        // 3. Generate JWT with role=ADMIN embedded as a claim
        String jwt = jwtUtil.generateToken(admin.getId(), admin.getEmail(), admin.getName());

        return AdminLoginResponseDTO.builder()
                .adminId(admin.getId())
                .name(admin.getName())
                .email(admin.getEmail())
                .token(jwt)
                .role(JwtUtil.ADMIN_ROLE)          // frontend checks: role === "ADMIN"
                .expiresIn(expirationMs / 1000)    // convert ms → seconds for the client
                .build();
    }

    // ── Token Validation ──────────────────────────────────────────────────

    /**
     * Called by AdminAuthInterceptor on every protected request.
     * Parses the JWT, checks the signature, expiry, and role=ADMIN.
     *
     * @param rawJwt the bare token string (without "Bearer " prefix)
     * @return the adminId (UUID) embedded as the JWT subject
     */
    @Override
    public UUID validateToken(String rawJwt) {
        try {
            Claims claims = jwtUtil.validateAndExtract(rawJwt);

            // Role check — only ADMIN tokens are accepted
            if (!jwtUtil.isAdminRole(claims)) {
                throw new UnauthorizedAdminException(
                        "Access denied: token does not have ADMIN role");
            }

            return jwtUtil.extractAdminId(claims);

        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedAdminException(
                    "Invalid or expired admin token: " + e.getMessage());
        }
    }
}
