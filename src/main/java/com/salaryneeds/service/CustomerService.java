package com.salaryneeds.service;

import com.salaryneeds.dto.CustomerCreateRequestDTO;
import com.salaryneeds.dto.CustomerLoginRequestDTO;
import com.salaryneeds.dto.CustomerLoginResponseDTO;
import com.salaryneeds.dto.CustomerResponseDTO;
import com.salaryneeds.dto.CustomerUpdateRequestDTO;
import com.salaryneeds.dto.PageResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    CustomerResponseDTO createCustomer(CustomerCreateRequestDTO request);

    CustomerResponseDTO getCustomerById(UUID customerId);

    List<CustomerResponseDTO> getAllCustomers();

    PageResponseDTO<CustomerResponseDTO> getCustomersPaginated(Pageable pageable);

    CustomerResponseDTO updateCustomer(UUID customerId, CustomerUpdateRequestDTO request);

    void deactivateCustomer(UUID customerId);

    void deleteCustomer(UUID customerId);

    CustomerLoginResponseDTO login(CustomerLoginRequestDTO request);
}

