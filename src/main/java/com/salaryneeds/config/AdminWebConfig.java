package com.salaryneeds.config;

import com.salaryneeds.security.AdminAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AdminWebConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;

    @org.springframework.beans.factory.annotation.Value("${cors.allowed-origins:https://api.anjibabujob.com,https://anjibabujob.com,https://*.anjibabujob.com,http://localhost:*,http://127.0.0.1:*,http://localhost:3000,http://localhost:5173,http://localhost:5174,http://localhost:5175,http://localhost:8081,http://localhost:8080}")
    private String allowedOrigins;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**", "/auth/**")
                .excludePathPatterns("/api/admin/auth/login", "/auth/login", "/api/auth/login");
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        String[] origins = (allowedOrigins != null && !allowedOrigins.isBlank())
                ? java.util.Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toArray(String[]::new)
                : new String[]{"*"};

        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
