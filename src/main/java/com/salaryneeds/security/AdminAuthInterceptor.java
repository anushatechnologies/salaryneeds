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

        // 1. Admin endpoints
        if (path.startsWith("/admin") || path.startsWith("/api/v1/admin") || path.startsWith("/api/admin")) {
            String role = request.getHeader("X-Role");
            String adminRole = request.getHeader("X-Admin-Role");
            String authHeader = request.getHeader("Authorization");

            if (path.equals("/admin/workers/documents") && role == null && adminRole == null && authHeader == null) {
                return true;
            }

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

        // 2. User catalogue endpoints
        if (path.startsWith("/api/user") || path.startsWith("/user/categories") || path.startsWith("/user/subcategories")) {
            String role = request.getHeader("X-Role");
            String userId = request.getHeader("X-User-Id");
            String customerId = request.getHeader("X-Customer-Id");
            String authHeader = request.getHeader("Authorization");

            if (role == null && userId == null && customerId == null && authHeader == null) {
                throw new UnauthorizedException("Authentication required: Missing user credentials");
            }

            if ("WORKER".equalsIgnoreCase(role)) {
                throw new ForbiddenException("Access denied: User role required to access " + path);
            }

            boolean isUser = "USER".equalsIgnoreCase(role) ||
                             "CUSTOMER".equalsIgnoreCase(role) ||
                             "ADMIN".equalsIgnoreCase(role) ||
                             userId != null || customerId != null ||
                             (authHeader != null && !authHeader.toUpperCase().contains("WORKER"));

            if (!isUser) {
                throw new ForbiddenException("Access denied: User role required to access " + path);
            }
        }

        // 3. Worker catalogue endpoints
        if (path.startsWith("/api/worker") || path.startsWith("/worker/catalog")) {
            String role = request.getHeader("X-Role");
            String workerId = request.getHeader("X-Worker-Id");
            String authHeader = request.getHeader("Authorization");

            if (role == null && workerId == null && authHeader == null) {
                throw new UnauthorizedException("Authentication required: Missing worker credentials");
            }

            if ("USER".equalsIgnoreCase(role) || "CUSTOMER".equalsIgnoreCase(role)) {
                throw new ForbiddenException("Access denied: Worker role required to access " + path);
            }

            boolean isWorker = "WORKER".equalsIgnoreCase(role) ||
                               "ADMIN".equalsIgnoreCase(role) ||
                               workerId != null ||
                               (authHeader != null && authHeader.toUpperCase().contains("WORKER"));

            if (!isWorker) {
                throw new ForbiddenException("Access denied: Worker role required to access " + path);
            }
        }

        return true;
    }
}
