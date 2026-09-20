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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CustomerResponseDTO createCustomer(CustomerCreateRequestDTO request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists: " + request.getEmail());
        }
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicatePhoneException("Phone already exists: " + request.getPhone());
        }

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : "Customer@123";
        String passwordHash = passwordEncoder.encode(rawPassword);

        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
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
    public CustomerResponseDTO getCustomerByPhone(String phone) {
        Customer customer = customerRepository.findByPhone(phone)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with phone: " + phone));
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
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (customer.getAccountStatus() != null &&
                ("INACTIVE".equalsIgnoreCase(customer.getAccountStatus()) ||
                 "SUSPENDED".equalsIgnoreCase(customer.getAccountStatus()) ||
                 "DEACTIVATED".equalsIgnoreCase(customer.getAccountStatus()))) {
            throw new UnauthorizedException("Account is " + customer.getAccountStatus().toLowerCase() + ". Please contact support.");
        }

        String rawPassword = request.getPassword();
        String storedHash = customer.getPasswordHash();
        boolean passwordMatches = false;

        if (storedHash != null && !storedHash.isBlank()) {
            if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
                passwordMatches = passwordEncoder.matches(rawPassword, storedHash);
            } else {
                // Fallback for plain-text password check if legacy/unhashed password exists
                passwordMatches = storedHash.equals(rawPassword) || passwordEncoder.matches(rawPassword, storedHash);
            }
        }

        if (!passwordMatches) {
            throw new UnauthorizedException("Invalid email or password");
        }

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
