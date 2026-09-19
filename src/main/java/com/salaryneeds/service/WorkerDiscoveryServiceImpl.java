package com.salaryneeds.service;

import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.WorkerProfileDTO;
import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.exception.WorkerNotFoundException;
import com.salaryneeds.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkerDiscoveryServiceImpl implements WorkerDiscoveryService {

    private final WorkerProfileRepository workerProfileRepository;

    @Override
    public PageResponseDTO<WorkerProfileDTO> searchWorkers(
            UUID categoryId,
            String service,
            String pincode,
            BigDecimal minRating,
            Boolean dutyOnline,
            Pageable pageable
    ) {
        Page<WorkerProfile> page = workerProfileRepository.searchWorkers(
                categoryId,
                service,
                pincode,
                minRating,
                dutyOnline,
                pageable
        );

        List<WorkerProfileDTO> content = page.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<WorkerProfileDTO>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    @Override
    public List<WorkerProfileDTO> getRecommendedWorkers(String pincode, UUID categoryId, int limit) {
        Pageable pageable = PageRequest.of(0, limit > 0 ? limit : 5);
        List<WorkerProfile> workers = workerProfileRepository.findRecommendedWorkers(pincode, categoryId, pageable);
        return workers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WorkerProfileDTO getWorkerProfile(UUID workerId) {
        WorkerProfile worker = workerProfileRepository.findById(workerId)
                .orElseThrow(() -> new WorkerNotFoundException("Worker profile not found with id: " + workerId));
        return mapToDTO(worker);
    }

    private WorkerProfileDTO mapToDTO(WorkerProfile worker) {
        return WorkerProfileDTO.builder()
                .id(worker.getId())
                .name(worker.getName())
                .email(worker.getEmail())
                .phone(worker.getPhone())
                .categoryId(worker.getCategory() != null ? worker.getCategory().getId() : null)
                .categoryName(worker.getCategory() != null ? worker.getCategory().getName() : null)
                .service(worker.getService())
                .skills(worker.getSkills())
                .experienceYears(worker.getExperienceYears())
                .pincode(worker.getPincode())
                .verified(worker.getVerified())
                .ratingAvg(worker.getRatingAvg())
                .completedJobsCount(worker.getCompletedJobsCount())
                .dutyOnline(worker.getDutyOnline())
                .lastLat(worker.getLastLat())
                .lastLng(worker.getLastLng())
                .lastSeenAt(worker.getLastSeenAt())
                .accountStatus(worker.getAccountStatus())
                .build();
    }
}
