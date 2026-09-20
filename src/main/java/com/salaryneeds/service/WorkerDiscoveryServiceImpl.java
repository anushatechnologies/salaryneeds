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
        org.springframework.data.jpa.domain.Specification<WorkerProfile> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (service != null && !service.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("service")), "%" + service.trim().toLowerCase() + "%"));
            }
            if (pincode != null && !pincode.isBlank()) {
                predicates.add(cb.equal(root.get("pincode"), pincode.trim()));
            }
            if (minRating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ratingAvg"), minRating));
            }
            if (dutyOnline != null) {
                predicates.add(cb.equal(root.get("dutyOnline"), dutyOnline));
            }
            predicates.add(cb.equal(root.get("accountStatus"), com.salaryneeds.entity.enums.AccountStatus.ACTIVE));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<WorkerProfile> page = workerProfileRepository.findAll(spec, pageable);

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
        return WorkerProfileDTO.fromEntity(worker);
    }
}
