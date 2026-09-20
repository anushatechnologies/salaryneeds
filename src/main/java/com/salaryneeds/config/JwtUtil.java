package com.salaryneeds.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT utility used exclusively for admin authentication.
 *
 * Token claims:
 *   sub   → adminId (UUID string)
 *   role  → "ADMIN"  ← role check done in interceptor
 *   email → admin email
 *   name  → admin display name
 */
@Component
public class JwtUtil {

    private static final String ROLE_CLAIM  = "role";
    private static final String EMAIL_CLAIM = "email";
    private static final String NAME_CLAIM  = "name";
    public  static final String ADMIN_ROLE  = "ADMIN";

    private final SecretKey secretKey;
    private final long      expirationMs;

    public JwtUtil(
            @Value("${admin.jwt.secret:default-secret-key-for-admin-jwt-must-be-at-least-256-bits-long-1234567890}") String secret,
            @Value("${admin.jwt.expiration-ms:86400000}") long expirationMs) {
        this.secretKey    = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // ── Token Generation ──────────────────────────────────────────────────

    public String generateToken(UUID adminId, String email, String name) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(adminId.toString())
                .claim(ROLE_CLAIM,  ADMIN_ROLE)
                .claim(EMAIL_CLAIM, email)
                .claim(NAME_CLAIM,  name)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    // ── Token Validation & Extraction ─────────────────────────────────────

    /**
     * Parses and validates the token.
     * Throws {@link JwtException} or {@link IllegalArgumentException} on failure.
     */
    public Claims validateAndExtract(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractAdminId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public String extractRole(Claims claims) {
        return claims.get(ROLE_CLAIM, String.class);
    }

    public boolean isAdminRole(Claims claims) {
        return ADMIN_ROLE.equals(extractRole(claims));
    }
}
