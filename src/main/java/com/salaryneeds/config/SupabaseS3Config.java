package com.salaryneeds.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Configuration for Supabase S3-compatible Storage.
 * Configures S3Client with custom endpoint, region, credentials, and path-style access.
 * Credentials are read from environment variables and never hardcoded or exposed to the client.
 */
@Configuration
public class SupabaseS3Config {

    @Value("${SUPABASE_S3_ENDPOINT:${supabase.s3.endpoint:https://jbgctbgqymrdurkumwes.storage.supabase.co/storage/v1/s3}}")
    private String endpoint;

    @Value("${SUPABASE_S3_REGION:${supabase.s3.region:ap-south-1}}")
    private String region;

    @Value("${SUPABASE_S3_ACCESS_KEY_ID:${supabase.s3.access-key-id:}}")
    private String accessKey;

    @Value("${SUPABASE_S3_SECRET_ACCESS_KEY:${supabase.s3.secret-access-key:}}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        String effectiveAccessKey = (accessKey != null && !accessKey.trim().isEmpty()) ? accessKey : "placeholder-access-key";
        String effectiveSecretKey = (secretKey != null && !secretKey.trim().isEmpty()) ? secretKey : "placeholder-secret-key";
        String effectiveRegion = (region != null && !region.trim().isEmpty()) ? region : "ap-south-1";

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(effectiveRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(effectiveAccessKey, effectiveSecretKey)
                ))
                .forcePathStyle(true)
                .build();
    }
}
