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
        String role = request.getHeader("X-Role");
        if (role == null) {
            role = "ADMIN"; // Default to open access
        }
        String adminIdHeader = request.getHeader("X-Admin-Id");
        if (adminIdHeader != null) {
            try {
                request.setAttribute("adminId", java.util.UUID.fromString(adminIdHeader));
            } catch (Exception ignored) {
            }
        }
        return true;
    }
}
