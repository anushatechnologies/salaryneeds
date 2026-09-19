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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns(
                        "/api/v1/admin/**",
                        "/api/admin/**",
                        "/admin/**",
                        "/api/user/**",
                        "/user/**",
                        "/api/worker/**",
                        "/worker/catalog/**"
                )
                .excludePathPatterns(
                        "/admin/workers/**",
                        "/admin/reviews/**",
                        "/worker/auth/**",
                        "/worker/profile/**",
                        "/worker/bookings/**",
                        "/worker/location/**",
                        "/worker/schedule/**",
                        "/worker/wallet/**",
                        "/worker/bank/**",
                        "/worker/documents/**",
                        "/worker/reviews/**",
                        "/worker/devices/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Customer-Id", "X-Worker-Id", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
