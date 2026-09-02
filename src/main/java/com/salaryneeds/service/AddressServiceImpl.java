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

        Boolean isDefault = request.getIsDefault() != null ? request.getIsDefault() : false;

        if (isDefault) {
            List<Address> existingDefaults = addressRepository.findByCustomerIdAndIsDefaultTrue(customerId);
            for (Address existingDefault : existingDefaults) {
                existingDefault.setIsDefault(false);
                addressRepository.save(existingDefault);
            }
        }

        Address address = Address.builder()
                .customer(customer)
                .label(request.getLabel())
                .addressLine(request.getAddressLine())
                .pincode(request.getPincode())
                .city(request.getCity())
                .isDefault(isDefault)
                .build();

        Address savedAddress = addressRepository.save(address);
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

        return addressRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AddressResponseDTO updateAddress(UUID customerId, UUID addressId, AddressUpdateRequestDTO request) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Address address = addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + addressId + " for customer: " + customerId));

        if (request.getLabel() != null) {
            address.setLabel(request.getLabel());
        }
        if (request.getAddressLine() != null) {
            address.setAddressLine(request.getAddressLine());
        }
        if (request.getPincode() != null) {
            address.setPincode(request.getPincode());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getIsDefault() != null) {
            if (request.getIsDefault() && !address.getIsDefault()) {
                List<Address> existingDefaults = addressRepository.findByCustomerIdAndIsDefaultTrue(customerId);
                for (Address existingDefault : existingDefaults) {
                    existingDefault.setIsDefault(false);
                    addressRepository.save(existingDefault);
                }
            }
            address.setIsDefault(request.getIsDefault());
        }

        Address updatedAddress = addressRepository.save(address);
        return mapToResponseDTO(updatedAddress);
    }

    @Override
    public void deleteAddress(UUID customerId, UUID addressId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Address address = addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + addressId + " for customer: " + customerId));

        addressRepository.delete(address);
    }

    @Override
    public void setDefaultAddress(UUID customerId, UUID addressId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Address address = addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + addressId + " for customer: " + customerId));

        List<Address> existingDefaults = addressRepository.findByCustomerIdAndIsDefaultTrue(customerId);
        for (Address existingDefault : existingDefaults) {
            existingDefault.setIsDefault(false);
            addressRepository.save(existingDefault);
        }

        address.setIsDefault(true);
        addressRepository.save(address);
    }

    private AddressResponseDTO mapToResponseDTO(Address address) {
        return AddressResponseDTO.builder()
                .id(address.getId())
                .label(address.getLabel())
                .addressLine(address.getAddressLine())
                .pincode(address.getPincode())
                .city(address.getCity())
                .isDefault(address.getIsDefault())
                .build();
    }

}
