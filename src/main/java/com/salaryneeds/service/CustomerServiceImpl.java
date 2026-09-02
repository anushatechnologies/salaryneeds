package com.salaryneeds.service;

import com.salaryneeds.dto.CustomerCreateRequestDTO;
import com.salaryneeds.dto.CustomerResponseDTO;
import com.salaryneeds.dto.CustomerUpdateRequestDTO;
import com.salaryneeds.entity.Customer;
import com.salaryneeds.exception.CustomerNotFoundException;
import com.salaryneeds.exception.DuplicateEmailException;
import com.salaryneeds.exception.DuplicatePhoneException;
import com.salaryneeds.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
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

        String passwordHash = passwordEncoder.encode(request.getPassword());

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
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerResponseDTO updateCustomer(UUID customerId, CustomerUpdateRequestDTO request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        if (request.getName() != null) {
            customer.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateEmailException("Email already exists: " + request.getEmail());
            }
            customer.setEmail(request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().equals(customer.getPhone())) {
            if (customerRepository.existsByPhone(request.getPhone())) {
                throw new DuplicatePhoneException("Phone already exists: " + request.getPhone());
            }
            customer.setPhone(request.getPhone());
        }
        if (request.getDefaultAddress() != null) {
            customer.setDefaultAddress(request.getDefaultAddress());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return mapToResponseDTO(updatedCustomer);
    }

    @Override
    public void deleteCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));
        customerRepository.delete(customer);
    }

    private CustomerResponseDTO mapToResponseDTO(Customer customer) {
        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .defaultAddress(customer.getDefaultAddress())
                .emailVerified(customer.getEmailVerified())
                .phoneVerified(customer.getPhoneVerified())
                .accountStatus(customer.getAccountStatus())
                .build();
    }

}
