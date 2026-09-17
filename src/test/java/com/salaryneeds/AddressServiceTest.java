package com.salaryneeds;

import com.salaryneeds.dto.AddressCreateRequestDTO;
import com.salaryneeds.dto.AddressResponseDTO;
import com.salaryneeds.dto.AddressUpdateRequestDTO;
import com.salaryneeds.entity.Address;
import com.salaryneeds.entity.Customer;
import com.salaryneeds.exception.AddressNotFoundException;
import com.salaryneeds.exception.CustomerNotFoundException;
import com.salaryneeds.repository.AddressRepository;
import com.salaryneeds.repository.CustomerRepository;
import com.salaryneeds.service.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private Customer sampleCustomer;
    private UUID customerId;
    private Address address1;
    private Address address2;
    private UUID address1Id;
    private UUID address2Id;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        address1Id = UUID.randomUUID();
        address2Id = UUID.randomUUID();

        sampleCustomer = Customer.builder()
                .id(customerId)
                .name("Pavan Kumar")
                .email("pavan@example.com")
                .phone("9876543210")
                .defaultAddress(null)
                .build();

        address1 = Address.builder()
                .id(address1Id)
                .customer(sampleCustomer)
                .label("Home")
                .house("Flat 101")
                .street("Main Road")
                .addressLine("Flat 101, Main Road")
                .city("Hyderabad")
                .pincode("500081")
                .isDefault(true)
                .build();

        address2 = Address.builder()
                .id(address2Id)
                .customer(sampleCustomer)
                .label("Work")
                .house("Tech Hub")
                .street("Hitech City")
                .addressLine("Tech Hub, Hitech City")
                .city("Hyderabad")
                .pincode("500081")
                .isDefault(false)
                .build();
    }

    @Test
    @DisplayName("Create first address - Automatically sets as default and syncs customer profile")
    void testCreateFirstAddress_SetsDefault() {
        AddressCreateRequestDTO request = AddressCreateRequestDTO.builder()
                .label("Home")
                .house("Flat 101")
                .street("Main Road")
                .city("Hyderabad")
                .pincode("500081")
                .isDefault(false)
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));
        when(addressRepository.countByCustomerId(customerId)).thenReturn(0L);
        when(addressRepository.findByCustomerIdAndIsDefaultTrue(customerId)).thenReturn(Collections.emptyList());
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address a = invocation.getArgument(0);
            a.setId(address1Id);
            return a;
        });

        AddressResponseDTO response = addressService.createAddress(customerId, request);

        assertNotNull(response);
        assertTrue(response.getIsDefault());
        assertNotNull(sampleCustomer.getDefaultAddress());
        assertTrue(sampleCustomer.getDefaultAddress().contains("Flat 101"));
        verify(customerRepository, times(1)).save(sampleCustomer);
    }

    @Test
    @DisplayName("Create new default address - Unsets previous defaults")
    void testCreateNewDefaultAddress_UnsetsPreviousDefault() {
        AddressCreateRequestDTO request = AddressCreateRequestDTO.builder()
                .label("Work")
                .house("Tech Hub")
                .street("Hitech City")
                .city("Hyderabad")
                .pincode("500081")
                .isDefault(true)
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));
        when(addressRepository.countByCustomerId(customerId)).thenReturn(1L);
        when(addressRepository.findByCustomerIdAndIsDefaultTrue(customerId)).thenReturn(List.of(address1));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddressResponseDTO response = addressService.createAddress(customerId, request);

        assertNotNull(response);
        assertTrue(response.getIsDefault());
        assertFalse(address1.getIsDefault()); // Previous default was unset
        verify(addressRepository, atLeastOnce()).save(address1);
    }

    @Test
    @DisplayName("Fallback Rule: Delete default address promotes next remaining address to default")
    void testDeleteDefaultAddress_FallbackPromotesNextAddress() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));
        when(addressRepository.findByIdAndCustomerId(address1Id, customerId)).thenReturn(Optional.of(address1));
        // Remaining list after deletion contains address2
        when(addressRepository.findByCustomerIdOrderByCreatedAtAsc(customerId)).thenReturn(List.of(address2));

        addressService.deleteAddress(customerId, address1Id);

        verify(addressRepository, times(1)).delete(address1);
        assertTrue(address2.getIsDefault()); // Next address promoted to default
        verify(addressRepository, times(1)).save(address2);
        assertNotNull(sampleCustomer.getDefaultAddress());
        assertTrue(sampleCustomer.getDefaultAddress().contains("Tech Hub"));
        verify(customerRepository, times(1)).save(sampleCustomer);
    }

    @Test
    @DisplayName("Delete last remaining address sets customer defaultAddress to null")
    void testDeleteLastAddress_ClearsCustomerDefault() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));
        when(addressRepository.findByIdAndCustomerId(address1Id, customerId)).thenReturn(Optional.of(address1));
        when(addressRepository.findByCustomerIdOrderByCreatedAtAsc(customerId)).thenReturn(Collections.emptyList());

        addressService.deleteAddress(customerId, address1Id);

        verify(addressRepository, times(1)).delete(address1);
        assertNull(sampleCustomer.getDefaultAddress());
        verify(customerRepository, times(1)).save(sampleCustomer);
    }

    @Test
    @DisplayName("Set default address explicitly")
    void testSetDefaultAddress_Explicit() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));
        when(addressRepository.findByIdAndCustomerId(address2Id, customerId)).thenReturn(Optional.of(address2));
        when(addressRepository.findByCustomerIdAndIsDefaultTrue(customerId)).thenReturn(List.of(address1));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddressResponseDTO response = addressService.setDefaultAddress(customerId, address2Id);

        assertNotNull(response);
        assertTrue(response.getIsDefault());
        assertFalse(address1.getIsDefault());
        assertTrue(address2.getIsDefault());
        assertNotNull(sampleCustomer.getDefaultAddress());
        assertTrue(sampleCustomer.getDefaultAddress().contains("Tech Hub"));
    }
}
