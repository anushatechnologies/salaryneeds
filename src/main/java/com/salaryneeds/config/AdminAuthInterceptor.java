package com.salaryneeds.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Authentication is disabled.
 * Requests are allowed through without requiring any Authorization header or token.
 * A default adminId is injected so audit logging remains operational.
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final UUID DEFAULT_ADMIN_ID = UUID.fromString("a0000000-0000-0000-0000-000000000001");

    @Override
    public boolean preHandle(HttpServletRequest  request,
                             HttpServletResponse response,
                             Object              handler) {
        // Allow all requests through without authentication
        request.setAttribute("adminId", DEFAULT_ADMIN_ID);
        return true;
    }
}
