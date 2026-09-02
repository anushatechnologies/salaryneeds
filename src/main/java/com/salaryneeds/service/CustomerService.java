package com.salaryneeds.service;

import com.salaryneeds.dto.CustomerCreateRequestDTO;
import com.salaryneeds.dto.CustomerResponseDTO;
import com.salaryneeds.dto.CustomerUpdateRequestDTO;
import java.util.List;
import java.util.UUID;

public interface CustomerService {

    CustomerResponseDTO createCustomer(CustomerCreateRequestDTO request);

    CustomerResponseDTO getCustomerById(UUID customerId);

    List<CustomerResponseDTO> getAllCustomers();

    CustomerResponseDTO updateCustomer(UUID customerId, CustomerUpdateRequestDTO request);

    void deleteCustomer(UUID customerId);

}
