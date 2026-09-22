package com.salaryneeds.controller;

import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.WorkerProfileDTO;
import com.salaryneeds.service.WorkerDiscoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/workers", "/api/worker", "/workers", "/worker"})
@RequiredArgsConstructor
public class WorkerDiscoveryController {

    private final WorkerDiscoveryService workerDiscoveryService;

    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<WorkerProfileDTO>> searchWorkers(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String pincode,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) Boolean dutyOnline,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ratingAvg") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponseDTO<WorkerProfileDTO> result = workerDiscoveryService.searchWorkers(
                categoryId, service, pincode, minRating, dutyOnline, pageable
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<WorkerProfileDTO>> getRecommendedWorkers(
            @RequestParam String pincode,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<WorkerProfileDTO> result = workerDiscoveryService.getRecommendedWorkers(pincode, categoryId, limit);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{workerId}/profile")
    public ResponseEntity<WorkerProfileDTO> getWorkerProfile(@PathVariable UUID workerId) {
        return ResponseEntity.ok(workerDiscoveryService.getWorkerProfile(workerId));
    }
}
