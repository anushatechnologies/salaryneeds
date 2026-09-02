package com.salaryneeds.service;

import com.salaryneeds.dto.AddressCreateRequestDTO;
import com.salaryneeds.dto.AddressResponseDTO;
import com.salaryneeds.dto.AddressUpdateRequestDTO;
import java.util.List;
import java.util.UUID;

public interface AddressService {

    AddressResponseDTO createAddress(UUID customerId, AddressCreateRequestDTO request);

    AddressResponseDTO getAddressById(UUID customerId, UUID addressId);

    List<AddressResponseDTO> getAllAddressesByCustomerId(UUID customerId);

    AddressResponseDTO updateAddress(UUID customerId, UUID addressId, AddressUpdateRequestDTO request);

    void deleteAddress(UUID customerId, UUID addressId);

    void setDefaultAddress(UUID customerId, UUID addressId);

}
