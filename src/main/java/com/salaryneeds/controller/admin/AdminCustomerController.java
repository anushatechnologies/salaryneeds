package com.salaryneeds.controller.admin;

import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.admin.AccountStatusUpdateRequestDTO;
import com.salaryneeds.dto.admin.AdminCustomerResponseDTO;
import com.salaryneeds.service.admin.AdminCustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final AdminCustomerService customerService;

    /** GET /api/admin/customers?page=0&size=20 */
    @GetMapping
    public ResponseEntity<PageResponseDTO<AdminCustomerResponseDTO>> getAllCustomers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(customerService.getAllCustomers(pageable));
    }

    /** GET /api/admin/customers/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<AdminCustomerResponseDTO> getCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    /** PATCH /api/admin/customers/{id}/status — Block / Unblock */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminCustomerResponseDTO> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AccountStatusUpdateRequestDTO req,
            HttpServletRequest request) {
        UUID adminId = (UUID) request.getAttribute("adminId");
        return ResponseEntity.ok(customerService.updateAccountStatus(id, req, adminId));
    }
}
