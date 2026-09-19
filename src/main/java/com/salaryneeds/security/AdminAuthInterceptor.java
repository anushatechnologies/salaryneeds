package com.salaryneeds.security;

import com.salaryneeds.exception.ForbiddenException;
import com.salaryneeds.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();

        // Only enforce for admin endpoints
        if (path.startsWith("/admin") || path.startsWith("/api/v1/admin") || path.startsWith("/api/admin")) {
            String role = request.getHeader("X-Role");
            String adminRole = request.getHeader("X-Admin-Role");
            String authHeader = request.getHeader("Authorization");

            if (role == null && adminRole == null && authHeader == null) {
                throw new UnauthorizedException("Authentication required: Missing admin credentials");
            }

            boolean isAdmin = "ADMIN".equalsIgnoreCase(role) ||
                              "ADMIN".equalsIgnoreCase(adminRole) ||
                              (authHeader != null && authHeader.toUpperCase().contains("ADMIN"));

            if (!isAdmin) {
                throw new ForbiddenException("Access denied: Administrator role required to access " + path);
            }
        }

        return true;
    }
}
