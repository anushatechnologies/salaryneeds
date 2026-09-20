package com.salaryneeds.service.admin;

import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.admin.AccountStatusUpdateRequestDTO;
import com.salaryneeds.dto.admin.AdminCustomerResponseDTO;
import com.salaryneeds.entity.Customer;
import com.salaryneeds.exception.CustomerNotFoundException;
import com.salaryneeds.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCustomerService {

    private final CustomerRepository customerRepo;
    private final AuditLogService    auditLogService;

    @Transactional(readOnly = true)
    public PageResponseDTO<AdminCustomerResponseDTO> getAllCustomers(Pageable pageable) {
        Page<Customer> page = customerRepo.findAllByOrderByCreatedAtDesc(pageable);
        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public AdminCustomerResponseDTO getCustomerById(UUID customerId) {
        Customer c = customerRepo.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId));
        return toDTO(c);
    }

    public AdminCustomerResponseDTO updateAccountStatus(UUID customerId,
                                                        AccountStatusUpdateRequestDTO req,
                                                        UUID adminId) {
        Customer c = customerRepo.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId));
        c.setAccountStatus(req.getAccountStatus());
        customerRepo.save(c);

        auditLogService.log(adminId, "UPDATE_CUSTOMER_STATUS", "CUSTOMER",
                customerId.toString(), "Status changed to " + req.getAccountStatus());
        return toDTO(c);
    }

    // ── Mapping ───────────────────────────────────────────────────────────

    private AdminCustomerResponseDTO toDTO(Customer c) {
        return AdminCustomerResponseDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .accountStatus(c.getAccountStatus())
                .emailVerified(c.getEmailVerified())
                .phoneVerified(c.getPhoneVerified())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private PageResponseDTO<AdminCustomerResponseDTO> toPageResponse(Page<Customer> page) {
        List<AdminCustomerResponseDTO> content = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return PageResponseDTO.<AdminCustomerResponseDTO>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}
