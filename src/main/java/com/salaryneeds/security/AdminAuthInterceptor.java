package com.salaryneeds.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.salaryneeds.config.JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ") && jwtUtil != null) {
            String token = authHeader.substring(7).trim();
            try {
                io.jsonwebtoken.Claims claims = jwtUtil.validateAndExtract(token);
                if (jwtUtil.isAdminRole(claims)) {
                    java.util.UUID adminId = jwtUtil.extractAdminId(claims);
                    request.setAttribute("adminId", adminId);
                    request.setAttribute("role", "ADMIN");
                }
            } catch (Exception ignored) {
            }
        }

        String role = request.getHeader("X-Role");
        if (role == null && request.getAttribute("role") == null) {
            role = "ADMIN"; // Default to open access
        }
        String adminIdHeader = request.getHeader("X-Admin-Id");
        if (adminIdHeader != null && request.getAttribute("adminId") == null) {
            try {
                request.setAttribute("adminId", java.util.UUID.fromString(adminIdHeader));
            } catch (Exception ignored) {
            }
        }
        return true;
    }
}
