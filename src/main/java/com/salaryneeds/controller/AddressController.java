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
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    private UUID resolveCustomerId(UUID pathId, String headerId, String paramId) {
        if (pathId != null) return pathId;
        String idStr = (headerId != null && !headerId.isBlank()) ? headerId.trim() : paramId;
        if (idStr != null && !idStr.isBlank()) {
            return UUID.fromString(idStr.trim());
        }
        return null;
    }

    @PostMapping({"/api/customers/{customerId}/addresses", "/api/addresses"})
    public ResponseEntity<AddressResponseDTO> createAddress(
            @PathVariable(required = false) UUID customerId,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam,
            @Valid @RequestBody AddressCreateRequestDTO request
    ) {
        UUID effectiveId = resolveCustomerId(customerId, customerIdHeader, customerIdParam);
        if (effectiveId == null) {
            throw new IllegalArgumentException("Customer ID is required in URL path, X-Customer-Id header, or customerId query parameter");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(effectiveId, request));
    }

    @GetMapping({"/api/customers/{customerId}/addresses", "/api/addresses"})
    public ResponseEntity<List<AddressResponseDTO>> getAllAddresses(
            @PathVariable(required = false) UUID customerId,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader,
            @RequestParam(value = "customerId", required = false) String customerIdParam
    ) {
        UUID effectiveId = resolveCustomerId(customerId, customerIdHeader, customerIdParam);
        if (effectiveId == null) {
            throw new IllegalArgumentException("Customer ID is required in URL path, X-Customer-Id header, or customerId query parameter");
        }
        return ResponseEntity.ok(addressService.getAllAddressesByCustomerId(effectiveId));
    }

    @GetMapping({"/api/customers/{customerId}/addresses/{addressId}", "/api/addresses/{addressId}"})
    public ResponseEntity<AddressResponseDTO> getAddressById(
            @PathVariable(required = false) UUID customerId,
            @PathVariable UUID addressId
    ) {
        return ResponseEntity.ok(addressService.getAddressById(customerId, addressId));
    }

    @PutMapping({"/api/customers/{customerId}/addresses/{addressId}", "/api/addresses/{addressId}"})
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable(required = false) UUID customerId,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressUpdateRequestDTO request
    ) {
        return ResponseEntity.ok(addressService.updateAddress(customerId, addressId, request));
    }

    @DeleteMapping({"/api/customers/{customerId}/addresses/{addressId}", "/api/addresses/{addressId}"})
    public ResponseEntity<Void> deleteAddress(
            @PathVariable(required = false) UUID customerId,
            @PathVariable UUID addressId
    ) {
        addressService.deleteAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping({"/api/customers/{customerId}/addresses/{addressId}/default", "/api/addresses/{addressId}/default"})
    public ResponseEntity<AddressResponseDTO> setDefaultAddressPut(
            @PathVariable(required = false) UUID customerId,
            @PathVariable UUID addressId
    ) {
        return ResponseEntity.ok(addressService.setDefaultAddress(customerId, addressId));
    }

    @PatchMapping({"/api/customers/{customerId}/addresses/{addressId}/default", "/api/addresses/{addressId}/default"})
    public ResponseEntity<AddressResponseDTO> setDefaultAddressPatch(
            @PathVariable(required = false) UUID customerId,
            @PathVariable UUID addressId
    ) {
        return ResponseEntity.ok(addressService.setDefaultAddress(customerId, addressId));
    }
}
