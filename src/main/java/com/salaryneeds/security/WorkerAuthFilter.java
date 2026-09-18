package com.salaryneeds.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class WorkerAuthFilter extends OncePerRequestFilter {


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/auth/") ||
               path.startsWith("/actuator") ||
               path.startsWith("/ws") ||
               path.startsWith("/error") ||
               path.startsWith("/admin/") ||
               path.startsWith("/catalog") ||
               path.startsWith("/v1/catalog") ||
               path.startsWith("/api/workers") ||
               path.startsWith("/api/admin/") ||
               path.equals("/") ||
               request.getMethod().equalsIgnoreCase("OPTIONS");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String workerIdHeader = request.getHeader("X-Worker-Id");
        String workerIdParam = request.getParameter("workerId");
        String workerId = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (!token.isBlank() && !token.startsWith("invalid") && !token.startsWith("bad")) {
                workerId = token;
            }
        } else if (authHeader != null && !authHeader.isBlank() && !authHeader.startsWith("invalid") && !authHeader.startsWith("bad")) {
            workerId = authHeader.trim();
        } else if (workerIdHeader != null && !workerIdHeader.isBlank() && !workerIdHeader.startsWith("invalid") && !workerIdHeader.startsWith("bad")) {
            workerId = workerIdHeader.trim();
        } else if (workerIdParam != null && !workerIdParam.isBlank()) {
            workerId = workerIdParam.trim();
        }

        if (workerId == null || workerId.isBlank()) {
            workerId = "w-default";
        }

        WorkerContext.setWorker(workerId, null);

        try {
            filterChain.doFilter(request, response);
        } finally {
            WorkerContext.clear();
        }
    }
}
