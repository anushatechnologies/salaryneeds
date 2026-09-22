package com.salaryneeds.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation = Paths.get("uploads");

    public FileStorageService() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.salaryneeds.service.storage.SupabaseStorageService supabaseStorageService;

    @org.springframework.beans.factory.annotation.Value("${app.api.base-url:${APP_API_BASE_URL:https://api.anjibabujob.com}}")
    private String apiBaseUrl;

    public String store(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (supabaseStorageService != null) {
            try {
                String cleanSub = (subDirectory != null) ? subDirectory.toLowerCase().trim() : "catalog";
                String s3Url;
                if (cleanSub.contains("categor") || cleanSub.contains("variant") || cleanSub.contains("catalog") || cleanSub.contains("service")) {
                    s3Url = supabaseStorageService.uploadCatalogAsset(cleanSub, file);
                } else {
                    s3Url = supabaseStorageService.uploadWorkerDocument("general", cleanSub, file);
                }
                if (s3Url != null && !s3Url.isBlank()) {
                    return s3Url;
                }
            } catch (Exception e) {
                // fall back to local disk
            }
        }

        try {
            Path targetDir = rootLocation.resolve(subDirectory != null ? subDirectory : "general");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = UUID.randomUUID() + extension;
            Path destinationFile = targetDir.resolve(filename).normalize().toAbsolutePath();

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + subDirectory + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public String storeFromUrl(String remoteUrl, String subDirectory) {
        if (remoteUrl == null || remoteUrl.isBlank()) {
            return null;
        }
        if (!remoteUrl.startsWith("http://") && !remoteUrl.startsWith("https://")) {
            return remoteUrl;
        }
        if (supabaseStorageService != null) {
            try {
                String cleanSub = (subDirectory != null) ? subDirectory.toLowerCase().trim() : "catalog";
                String s3Url = supabaseStorageService.uploadFromUrl(cleanSub, remoteUrl);
                if (s3Url != null && !s3Url.isBlank()) {
                    return s3Url;
                }
            } catch (Exception e) {
                // Return original URL if remote fetch fails
            }
        }
        return remoteUrl;
    }
}
