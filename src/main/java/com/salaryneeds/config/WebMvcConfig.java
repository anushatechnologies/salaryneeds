package com.salaryneeds.config;

import com.salaryneeds.security.AdminAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;

    @org.springframework.beans.factory.annotation.Value("${cors.allowed-origins:https://api.anjibabujob.com,https://anjibabujob.com,https://*.anjibabujob.com,http://localhost:3000,http://localhost:5173,http://localhost:8081,http://localhost:8080}")
    private String allowedOrigins;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**", "/api/v1/admin/**", "/api/admin/**", "/api/user/**", "/user/**", "/api/worker/**", "/worker/catalog/**")
                .excludePathPatterns(
                        "/api/admin/auth/**",
                        "/auth/**",
                        "/api/auth/**",
                        "/api/worker/signup",
                        "/api/worker/register",
                        "/api/worker/login",
                        "/api/worker/verify-otp",
                        "/api/worker/send-otp",
                        "/api/worker/sendOtp",
                        "/api/worker/check-phone",
                        "/api/worker/check-aadhar",
                        "/api/worker/check-pan",
                        "/worker/signup",
                        "/worker/login",
                        "/worker/send-otp"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = (allowedOrigins != null && !allowedOrigins.isBlank())
                ? java.util.Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toArray(String[]::new)
                : new String[]{"*"};

        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Customer-Id", "X-Worker-Id", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}

