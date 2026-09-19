package com.salaryneeds.service;

import com.salaryneeds.dto.*;
import com.salaryneeds.entity.Address;
import com.salaryneeds.entity.Customer;
import com.salaryneeds.exception.CustomerNotFoundException;
import com.salaryneeds.exception.DuplicateEmailException;
import com.salaryneeds.exception.DuplicatePhoneException;
import com.salaryneeds.exception.UnauthorizedException;
import com.salaryneeds.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CustomerResponseDTO createCustomer(CustomerCreateRequestDTO request) {
        String normalizedEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        String normalizedPhone = request.getPhone() != null ? request.getPhone().trim() : "";

        if (customerRepository.existsByNormalizedEmail(normalizedEmail)) {
            throw new DuplicateEmailException("Email already exists: " + normalizedEmail);
        }
        if (customerRepository.existsByNormalizedPhone(normalizedPhone)) {
            throw new DuplicatePhoneException("Phone already exists: " + normalizedPhone);
        }

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword().trim()
                : "Customer@123";
        String passwordHash = passwordEncoder.encode(rawPassword);

        Customer customer = Customer.builder()
                .name(request.getName() != null ? request.getName().trim() : "")
                .email(normalizedEmail)
                .phone(normalizedPhone)
                .passwordHash(passwordHash)
                .defaultAddress(request.getDefaultAddress())
                .emailVerified(false)
                .phoneVerified(false)
                .accountStatus("ACTIVE")
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        return mapToResponseDTO(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDTO getCustomerById(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));
        return mapToResponseDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CustomerResponseDTO> getCustomersPaginated(Pageable pageable) {
        Page<Customer> page = customerRepository.findAll(pageable);
        List<CustomerResponseDTO> content = page.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<CustomerResponseDTO>builder()
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
    public CustomerResponseDTO updateCustomer(UUID customerId, CustomerUpdateRequestDTO request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        if (request.getName() != null && !request.getName().isBlank()) {
            customer.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank() && !request.getEmail().equalsIgnoreCase(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateEmailException("Email already exists: " + request.getEmail());
            }
            customer.setEmail(request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank() && !request.getPhone().equals(customer.getPhone())) {
            if (customerRepository.existsByPhone(request.getPhone())) {
                throw new DuplicatePhoneException("Phone already exists: " + request.getPhone());
            }
            customer.setPhone(request.getPhone());
        }
        if (request.getDefaultAddress() != null) {
            customer.setDefaultAddress(request.getDefaultAddress());
        }
        if (request.getAccountStatus() != null && !request.getAccountStatus().isBlank()) {
            customer.setAccountStatus(request.getAccountStatus().toUpperCase());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            customer.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return mapToResponseDTO(updatedCustomer);
    }

    @Override
    public void deactivateCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));
        customer.setAccountStatus("INACTIVE");
        customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));
        customerRepository.delete(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerLoginResponseDTO login(CustomerLoginRequestDTO request) {
        String identifier = request.getEmail() != null ? request.getEmail().trim() : "";
        String normalizedIdentifier = identifier.toLowerCase();
        log.info("Customer login endpoint reached for identifier: {}", normalizedIdentifier);

        Optional<Customer> customerOpt = customerRepository.findByEmailOrPhoneNormalized(identifier);
        if (customerOpt.isEmpty()) {
            customerOpt = customerRepository.findByEmailIgnoreCase(identifier);
        }
        if (customerOpt.isEmpty()) {
            customerOpt = customerRepository.findByPhone(identifier);
        }

        boolean customerFound = customerOpt.isPresent();
        log.info("Customer record found: {}", customerFound);

        if (!customerFound) {
            throw new UnauthorizedException("Invalid email or password");
        }

        Customer customer = customerOpt.get();

        if (customer.getAccountStatus() != null &&
                ("INACTIVE".equalsIgnoreCase(customer.getAccountStatus()) ||
                 "SUSPENDED".equalsIgnoreCase(customer.getAccountStatus()) ||
                 "DEACTIVATED".equalsIgnoreCase(customer.getAccountStatus()))) {
            log.warn("Customer login rejected due to account status: {}", customer.getAccountStatus());
            throw new UnauthorizedException("Account is " + customer.getAccountStatus().toLowerCase() + ". Please contact support.");
        }

        String rawPassword = request.getPassword() != null ? request.getPassword() : "";
        String storedHash = customer.getPasswordHash() != null ? customer.getPasswordHash().trim() : "";
        boolean passwordMatches = false;

        if (!storedHash.isBlank()) {
            if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
                passwordMatches = passwordEncoder.matches(rawPassword, storedHash)
                        || passwordEncoder.matches(rawPassword.trim(), storedHash);
            } else {
                // Fallback for plain-text password match if legacy unhashed password exists in database
                passwordMatches = storedHash.equals(rawPassword)
                        || storedHash.equals(rawPassword.trim())
                        || passwordEncoder.matches(rawPassword, storedHash);
            }
        }

        log.info("Password match succeeded: {}", passwordMatches);

        if (!passwordMatches) {
            throw new UnauthorizedException("Invalid email or password");
        }

        log.info("Customer login successful for identifier: {}", normalizedIdentifier);

        return CustomerLoginResponseDTO.builder()
                .success(true)
                .message("Login successful")
                .customer(mapToResponseDTO(customer))
                .build();
    }

    private CustomerResponseDTO mapToResponseDTO(Customer customer) {
        List<AddressResponseDTO> addresses = null;
        if (customer.getAddresses() != null) {
            addresses = customer.getAddresses().stream()
                    .map(this::mapAddressToDTO)
                    .collect(Collectors.toList());
        }

        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .defaultAddress(customer.getDefaultAddress())
                .emailVerified(customer.getEmailVerified())
                .phoneVerified(customer.getPhoneVerified())
                .accountStatus(customer.getAccountStatus())
                .addresses(addresses)
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    private AddressResponseDTO mapAddressToDTO(Address address) {
        return AddressResponseDTO.builder()
                .id(address.getId())
                .customerId(address.getCustomer() != null ? address.getCustomer().getId() : null)
                .label(address.getLabel())
                .house(address.getHouse())
                .street(address.getStreet())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .pincode(address.getPincode())
                .lat(address.getLat())
                .lng(address.getLng())
                .isDefault(address.getIsDefault())
                .formattedAddress(address.toFormattedAddress())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
