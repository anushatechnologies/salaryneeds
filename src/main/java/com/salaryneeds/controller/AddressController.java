package com.salaryneeds.controller;

import com.salaryneeds.dto.AddressCreateRequestDTO;
import com.salaryneeds.dto.AddressResponseDTO;
import com.salaryneeds.dto.AddressUpdateRequestDTO;
import com.salaryneeds.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/{customerId}/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponseDTO> createAddress(
            @PathVariable UUID customerId,
            @Valid @RequestBody AddressCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(customerId, request));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getAllAddresses(@PathVariable UUID customerId) {
        return ResponseEntity.ok(addressService.getAllAddressesByCustomerId(customerId));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponseDTO> getAddressById(
            @PathVariable UUID customerId,
            @PathVariable UUID addressId) {
        return ResponseEntity.ok(addressService.getAddressById(customerId, addressId));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable UUID customerId,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressUpdateRequestDTO request) {
        return ResponseEntity.ok(addressService.updateAddress(customerId, addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable UUID customerId,
            @PathVariable UUID addressId) {
        addressService.deleteAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefaultAddress(
            @PathVariable UUID customerId,
            @PathVariable UUID addressId) {
        addressService.setDefaultAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }

}
