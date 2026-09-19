package com.salaryneeds.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            try {
                // Auto-repair schema history table to resolve checksum mismatches from modified scripts
                flyway.repair();
            } catch (Exception e) {
                // Ignore repair errors if any
            }
            // Run pending migrations
            flyway.migrate();
        };
    }
}
