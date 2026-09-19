package com.salaryneeds.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            log.info("Running Flyway repair to synchronize migration checksums with database...");
            try {
                flyway.repair();
                log.info("Flyway repair completed successfully.");
            } catch (Exception e) {
                log.warn("Flyway repair warning, proceeding with migrate: {}", e.getMessage());
            }
            log.info("Running Flyway migrate...");
            flyway.migrate();
            log.info("Flyway migration completed.");
        };
    }
}
