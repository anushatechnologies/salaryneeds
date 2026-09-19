package com.salaryneeds.service;

import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.WorkerProfileDTO;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface WorkerDiscoveryService {

    PageResponseDTO<WorkerProfileDTO> searchWorkers(
            UUID categoryId,
            String service,
            String pincode,
            BigDecimal minRating,
            Boolean dutyOnline,
            Pageable pageable
    );

    List<WorkerProfileDTO> getRecommendedWorkers(String pincode, UUID categoryId, int limit);

    WorkerProfileDTO getWorkerProfile(UUID workerId);
}
