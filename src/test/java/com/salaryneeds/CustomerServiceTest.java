package com.salaryneeds;

import com.salaryneeds.dto.*;
import com.salaryneeds.entity.Customer;
import com.salaryneeds.exception.CustomerNotFoundException;
import com.salaryneeds.exception.DuplicateEmailException;
import com.salaryneeds.exception.DuplicatePhoneException;
import com.salaryneeds.exception.UnauthorizedException;
import com.salaryneeds.repository.CustomerRepository;
import com.salaryneeds.service.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer sampleCustomer;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        sampleCustomer = Customer.builder()
                .id(customerId)
                .name("Pavan Kumar")
                .email("pavan@example.com")
                .phone("9876543210")
                .passwordHash("hashedSecret")
                .defaultAddress("Flat 101, Madhapur, Hyderabad")
                .emailVerified(false)
                .phoneVerified(false)
                .accountStatus("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("Create customer - Success")
    void testCreateCustomer_Success() {
        CustomerCreateRequestDTO request = CustomerCreateRequestDTO.builder()
                .name("Pavan Kumar")
                .email("pavan@example.com")
                .phone("9876543210")
                .password("Password@123")
                .defaultAddress("Flat 101, Madhapur, Hyderabad")
                .build();

        when(customerRepository.existsByEmail("pavan@example.com")).thenReturn(false);
        when(customerRepository.existsByPhone("9876543210")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashedSecret");
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

        CustomerResponseDTO response = customerService.createCustomer(request);

        assertNotNull(response);
        assertEquals(customerId, response.getId());
        assertEquals("Pavan Kumar", response.getName());
        assertEquals("pavan@example.com", response.getEmail());
        assertEquals("ACTIVE", response.getAccountStatus());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Create customer - Duplicate Email throws DuplicateEmailException")
    void testCreateCustomer_DuplicateEmail() {
        CustomerCreateRequestDTO request = CustomerCreateRequestDTO.builder()
                .name("Pavan Kumar")
                .email("pavan@example.com")
                .phone("9876543210")
                .password("Password@123")
                .build();

        when(customerRepository.existsByEmail("pavan@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> customerService.createCustomer(request));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Create customer - Duplicate Phone throws DuplicatePhoneException")
    void testCreateCustomer_DuplicatePhone() {
        CustomerCreateRequestDTO request = CustomerCreateRequestDTO.builder()
                .name("Pavan Kumar")
                .email("pavan@example.com")
                .phone("9876543210")
                .password("Password@123")
                .build();

        when(customerRepository.existsByEmail("pavan@example.com")).thenReturn(false);
        when(customerRepository.existsByPhone("9876543210")).thenReturn(true);

        assertThrows(DuplicatePhoneException.class, () -> customerService.createCustomer(request));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Get customer by ID - Success")
    void testGetCustomerById_Success() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));

        CustomerResponseDTO response = customerService.getCustomerById(customerId);

        assertNotNull(response);
        assertEquals(customerId, response.getId());
        assertEquals("Pavan Kumar", response.getName());
    }

    @Test
    @DisplayName("Get customer by ID - Not Found throws CustomerNotFoundException")
    void testGetCustomerById_NotFound() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(customerId));
    }

    @Test
    @DisplayName("Update customer profile - Success")
    void testUpdateCustomer_Success() {
        CustomerUpdateRequestDTO request = CustomerUpdateRequestDTO.builder()
                .name("Pavan Updated")
                .defaultAddress("New Address, Hyderabad")
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

        CustomerResponseDTO response = customerService.updateCustomer(customerId, request);

        assertNotNull(response);
        assertEquals("Pavan Updated", sampleCustomer.getName());
        assertEquals("New Address, Hyderabad", sampleCustomer.getDefaultAddress());
    }

    @Test
    @DisplayName("Deactivate customer - sets accountStatus to INACTIVE")
    void testDeactivateCustomer_Success() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

        customerService.deactivateCustomer(customerId);

        assertEquals("INACTIVE", sampleCustomer.getAccountStatus());
        verify(customerRepository, times(1)).save(sampleCustomer);
    }

    @Test
    @DisplayName("Customer Login - Success with correct email and password")
    void testLogin_Success() {
        CustomerLoginRequestDTO request = CustomerLoginRequestDTO.builder()
                .email("pavan@example.com")
                .password("Password@123")
                .build();

        sampleCustomer.setPasswordHash("$2a$10$someHashedPassword");
        when(customerRepository.findByEmail("pavan@example.com")).thenReturn(Optional.of(sampleCustomer));
        when(passwordEncoder.matches("Password@123", "$2a$10$someHashedPassword")).thenReturn(true);

        CustomerLoginResponseDTO response = customerService.login(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getCustomer());
        assertEquals("Pavan Kumar", response.getCustomer().getName());
        assertEquals("pavan@example.com", response.getCustomer().getEmail());
    }

    @Test
    @DisplayName("Customer Login - Email not found throws UnauthorizedException")
    void testLogin_EmailNotFound() {
        CustomerLoginRequestDTO request = CustomerLoginRequestDTO.builder()
                .email("unknown@example.com")
                .password("Password@123")
                .build();

        when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> customerService.login(request));
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    @DisplayName("Customer Login - Wrong password throws UnauthorizedException")
    void testLogin_WrongPassword() {
        CustomerLoginRequestDTO request = CustomerLoginRequestDTO.builder()
                .email("pavan@example.com")
                .password("WrongPassword")
                .build();

        sampleCustomer.setPasswordHash("$2a$10$someHashedPassword");
        when(customerRepository.findByEmail("pavan@example.com")).thenReturn(Optional.of(sampleCustomer));
        when(passwordEncoder.matches("WrongPassword", "$2a$10$someHashedPassword")).thenReturn(false);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> customerService.login(request));
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    @DisplayName("Customer Login - Inactive account throws UnauthorizedException")
    void testLogin_InactiveAccount() {
        CustomerLoginRequestDTO request = CustomerLoginRequestDTO.builder()
                .email("pavan@example.com")
                .password("Password@123")
                .build();

        sampleCustomer.setAccountStatus("INACTIVE");
        when(customerRepository.findByEmail("pavan@example.com")).thenReturn(Optional.of(sampleCustomer));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> customerService.login(request));
        assertTrue(exception.getMessage().contains("inactive"));
    }
}
