package com.salaryneeds.service;

import com.salaryneeds.dto.AddressCreateRequestDTO;
import com.salaryneeds.dto.AddressResponseDTO;
import com.salaryneeds.dto.AddressUpdateRequestDTO;
import com.salaryneeds.entity.Address;
import com.salaryneeds.entity.Customer;
import com.salaryneeds.exception.AddressNotFoundException;
import com.salaryneeds.exception.CustomerNotFoundException;
import com.salaryneeds.repository.AddressRepository;
import com.salaryneeds.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    @Override
    public AddressResponseDTO createAddress(UUID customerId, AddressCreateRequestDTO request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        long existingCount = addressRepository.countByCustomerId(customerId);
        // If this is the first address, or if explicitly requested as default
        boolean isDefault = (existingCount == 0) || Boolean.TRUE.equals(request.getIsDefault());

        if (isDefault) {
            unsetDefaultAddresses(customerId);
        }

        String label = (request.getLabel() != null && !request.getLabel().isBlank()) ? request.getLabel() : "Home";
        String addressLine = request.resolveAddressLine();

        Address address = Address.builder()
                .customer(customer)
                .label(label)
                .house(request.getHouse())
                .street(request.getStreet())
                .addressLine(addressLine)
                .city(request.getCity())
                .pincode(request.getPincode())
                .lat(request.getLat())
                .lng(request.getLng())
                .isDefault(isDefault)
                .build();

        Address savedAddress = addressRepository.save(address);

        if (isDefault) {
            customer.setDefaultAddress(savedAddress.toFormattedAddress());
            customerRepository.save(customer);
        }

        return mapToResponseDTO(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponseDTO getAddressById(UUID customerId, UUID addressId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Address address = addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + addressId + " for customer: " + customerId));

        return mapToResponseDTO(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getAllAddressesByCustomerId(UUID customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        return addressRepository.findByCustomerIdOrderByCreatedAtAsc(customerId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AddressResponseDTO updateAddress(UUID customerId, UUID addressId, AddressUpdateRequestDTO request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Address address = addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + addressId + " for customer: " + customerId));

        if (request.getLabel() != null && !request.getLabel().isBlank()) {
            address.setLabel(request.getLabel());
        }
        if (request.getHouse() != null) {
            address.setHouse(request.getHouse());
        }
        if (request.getStreet() != null) {
            address.setStreet(request.getStreet());
        }
        if (request.getAddressLine() != null && !request.getAddressLine().isBlank()) {
            address.setAddressLine(request.getAddressLine());
        } else if (request.resolveAddressLine() != null) {
            address.setAddressLine(request.resolveAddressLine());
        }
        if (request.getCity() != null && !request.getCity().isBlank()) {
            address.setCity(request.getCity());
        }
        if (request.getPincode() != null && !request.getPincode().isBlank()) {
            address.setPincode(request.getPincode());
        }
        if (request.getLat() != null) {
            address.setLat(request.getLat());
        }
        if (request.getLng() != null) {
            address.setLng(request.getLng());
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetDefaultAddresses(customerId);
            address.setIsDefault(true);
            customer.setDefaultAddress(address.toFormattedAddress());
            customerRepository.save(customer);
        }

        Address updatedAddress = addressRepository.save(address);
        return mapToResponseDTO(updatedAddress);
    }

    @Override
    public void deleteAddress(UUID customerId, UUID addressId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Address address = addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + addressId + " for customer: " + customerId));

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());

        addressRepository.delete(address);
        addressRepository.flush();

        // Fallback Rule: If the deleted address was marked as default, promote next available address
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByCustomerIdOrderByCreatedAtAsc(customerId);
            if (!remainingAddresses.isEmpty()) {
                Address newDefaultAddress = remainingAddresses.get(0);
                newDefaultAddress.setIsDefault(true);
                addressRepository.save(newDefaultAddress);
                customer.setDefaultAddress(newDefaultAddress.toFormattedAddress());
            } else {
                customer.setDefaultAddress(null);
            }
            customerRepository.save(customer);
        }
    }

    @Override
    public AddressResponseDTO setDefaultAddress(UUID customerId, UUID addressId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Address address = addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + addressId + " for customer: " + customerId));

        unsetDefaultAddresses(customerId);
        address.setIsDefault(true);
        Address saved = addressRepository.save(address);

        customer.setDefaultAddress(saved.toFormattedAddress());
        customerRepository.save(customer);

        return mapToResponseDTO(saved);
    }

    private void unsetDefaultAddresses(UUID customerId) {
        List<Address> defaults = addressRepository.findByCustomerIdAndIsDefaultTrue(customerId);
        for (Address addr : defaults) {
            addr.setIsDefault(false);
            addressRepository.save(addr);
        }
    }

    private AddressResponseDTO mapToResponseDTO(Address address) {
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
