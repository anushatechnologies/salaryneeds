package com.salaryneeds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaryneeds.controller.*;
import com.salaryneeds.dto.*;
import com.salaryneeds.entity.enums.BookingStatus;
import com.salaryneeds.entity.enums.DiscountType;
import com.salaryneeds.exception.*;
import com.salaryneeds.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AllApisEdgeCaseIntegrationTest {

    private MockMvc customerMockMvc;
    private MockMvc addressMockMvc;
    private MockMvc bookingMockMvc;
    private MockMvc couponMockMvc;
    private MockMvc catalogMockMvc;
    private MockMvc workerMockMvc;

    @Mock
    private CustomerService customerService;

    @Mock
    private AddressService addressService;

    @Mock
    private BookingService bookingService;

    @Mock
    private CouponService couponService;

    @Mock
    private CatalogManagementService catalogManagementService;

    @Mock
    private WorkerDiscoveryService workerDiscoveryService;

    @InjectMocks
    private CustomerController customerController;

    @InjectMocks
    private AddressController addressController;

    @InjectMocks
    private BookingController bookingController;

    @InjectMocks
    private CouponController couponController;

    @InjectMocks
    private AdminCatalogController adminCatalogController;

    @InjectMocks
    private WorkerDiscoveryController workerDiscoveryController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final UUID customerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID addressId1 = UUID.fromString("a1111111-1111-1111-1111-111111111111");
    private final UUID addressId2 = UUID.fromString("a2222222-2222-2222-2222-222222222222");
    private final UUID categoryId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID workerId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();

        customerMockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        addressMockMvc = MockMvcBuilders.standaloneSetup(addressController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        bookingMockMvc = MockMvcBuilders.standaloneSetup(bookingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        couponMockMvc = MockMvcBuilders.standaloneSetup(couponController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        catalogMockMvc = MockMvcBuilders.standaloneSetup(adminCatalogController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        workerMockMvc = MockMvcBuilders.standaloneSetup(workerDiscoveryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ==========================================
    // DOMAIN 1: CUSTOMER & ADDRESS EDGE CASES
    // ==========================================

    @Test
    @DisplayName("Edge Case 1.1: Register customer - Success (201 Created)")
    void testRegisterCustomer_Success() throws Exception {
        CustomerCreateRequestDTO request = CustomerCreateRequestDTO.builder()
                .name("Ananya Sharma")
                .email("ananya@example.com")
                .phone("9876543299")
                .password("Secret@123")
                .defaultAddress("Flat 302, Gachibowli")
                .build();

        CustomerResponseDTO response = CustomerResponseDTO.builder()
                .id(customerId)
                .name("Ananya Sharma")
                .email("ananya@example.com")
                .phone("9876543299")
                .defaultAddress("Flat 302, Gachibowli")
                .emailVerified(false)
                .phoneVerified(false)
                .accountStatus("ACTIVE")
                .build();

        when(customerService.createCustomer(any())).thenReturn(response);

        MvcResult result = customerMockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.name").value("Ananya Sharma"))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                .andReturn();

        System.out.println("\n[RESPONSE 1.1] Register Customer:\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 1.2: Register customer - Duplicate Email (409 Conflict)")
    void testRegisterCustomer_DuplicateEmail() throws Exception {
        CustomerCreateRequestDTO request = CustomerCreateRequestDTO.builder()
                .name("Ananya Sharma")
                .email("duplicate@example.com")
                .phone("9876543299")
                .password("Secret@123")
                .build();

        when(customerService.createCustomer(any()))
                .thenThrow(new DuplicateEmailException("Email already exists: duplicate@example.com"));

        MvcResult result = customerMockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicate Email"))
                .andExpect(jsonPath("$.message").value("Email already exists: duplicate@example.com"))
                .andReturn();

        System.out.println("\n[RESPONSE 1.2] Duplicate Email Error:\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 1.3: Register customer - Validation failure for invalid email & phone (400 Bad Request)")
    void testRegisterCustomer_ValidationFailure() throws Exception {
        CustomerCreateRequestDTO invalidRequest = CustomerCreateRequestDTO.builder()
                .name("")
                .email("not-an-email")
                .phone("123") // too short
                .password("") // blank password
                .build();

        MvcResult result = customerMockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andReturn();

        System.out.println("\n[RESPONSE 1.3] Validation Failure with Field Errors:\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 1.4: Get non-existent customer (404 Not Found)")
    void testGetCustomer_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        when(customerService.getCustomerById(randomId))
                .thenThrow(new CustomerNotFoundException("Customer not found with id: " + randomId));

        MvcResult result = customerMockMvc.perform(get("/api/customers/" + randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Customer Not Found"))
                .andReturn();

        System.out.println("\n[RESPONSE 1.4] Customer Not Found Error:\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 1.5: Deactivate customer account (204 No Content)")
    void testDeactivateCustomer() throws Exception {
        doNothing().when(customerService).deactivateCustomer(customerId);

        MvcResult result = customerMockMvc.perform(delete("/api/customers/" + customerId))
                .andExpect(status().isNoContent())
                .andReturn();

        System.out.println("\n[RESPONSE 1.5] Deactivate Customer: HTTP " + result.getResponse().getStatus());
    }

    @Test
    @DisplayName("Edge Case 1.6: Add new default address (201 Created)")
    void testAddDefaultAddress_Success() throws Exception {
        AddressCreateRequestDTO request = AddressCreateRequestDTO.builder()
                .label("Home")
                .house("Flat 402, Lotus Heights")
                .street("Ayyappa Society Main Road")
                .city("Hyderabad")
                .pincode("500081")
                .isDefault(true)
                .build();

        AddressResponseDTO response = AddressResponseDTO.builder()
                .id(addressId1)
                .customerId(customerId)
                .label("Home")
                .house("Flat 402, Lotus Heights")
                .street("Ayyappa Society Main Road")
                .city("Hyderabad")
                .pincode("500081")
                .isDefault(true)
                .formattedAddress("Flat 402, Lotus Heights, Ayyappa Society Main Road, Hyderabad - 500081")
                .build();

        when(addressService.createAddress(eq(customerId), any())).thenReturn(response);

        MvcResult result = addressMockMvc.perform(post("/api/customers/" + customerId + "/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isDefault").value(true))
                .andExpect(jsonPath("$.formattedAddress").exists())
                .andReturn();

        System.out.println("\n[RESPONSE 1.6] Add Address:\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 1.7: Explicitly mark address as default (200 OK)")
    void testSetDefaultAddress_PutAndPatch() throws Exception {
        AddressResponseDTO response = AddressResponseDTO.builder()
                .id(addressId2)
                .customerId(customerId)
                .label("Work")
                .isDefault(true)
                .formattedAddress("Cyber Towers, Hitech City, Hyderabad - 500081")
                .build();

        when(addressService.setDefaultAddress(customerId, addressId2)).thenReturn(response);

        MvcResult result = addressMockMvc.perform(put("/api/customers/" + customerId + "/addresses/" + addressId2 + "/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDefault").value(true))
                .andReturn();

        System.out.println("\n[RESPONSE 1.7] Set Default Address (PUT):\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 1.8: Delete address (204 No Content)")
    void testDeleteAddress() throws Exception {
        doNothing().when(addressService).deleteAddress(customerId, addressId1);

        MvcResult result = addressMockMvc.perform(delete("/api/customers/" + customerId + "/addresses/" + addressId1))
                .andExpect(status().isNoContent())
                .andReturn();

        System.out.println("\n[RESPONSE 1.8] Delete Address: HTTP " + result.getResponse().getStatus());
    }

    // ==========================================
    // DOMAIN 2: SLOTS, COUPONS & BOOKING EDGE CASES
    // ==========================================

    @Test
    @DisplayName("Edge Case 2.1: Dynamic slots for future date (200 OK - 4 slots available)")
    void testGetSlots_FutureDate() throws Exception {
        List<SlotResponseDTO> slots = List.of(
                SlotResponseDTO.builder().slotId("SLOT-0912").timeRange("09:00 AM - 12:00 PM").isAvailable(true).message("Available").build(),
                SlotResponseDTO.builder().slotId("SLOT-1215").timeRange("12:00 PM - 03:00 PM").isAvailable(true).message("Available").build(),
                SlotResponseDTO.builder().slotId("SLOT-1518").timeRange("03:00 PM - 06:00 PM").isAvailable(true).message("Available").build(),
                SlotResponseDTO.builder().slotId("SLOT-1821").timeRange("06:00 PM - 09:00 PM").isAvailable(true).message("Available").build()
        );

        when(bookingService.getAvailableSlots(eq(1L), any())).thenReturn(slots);

        MvcResult result = bookingMockMvc.perform(get("/api/bookings/slots?service_id=1&date=2026-09-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].slotId").value("SLOT-0912"))
                .andExpect(jsonPath("$[0].isAvailable").value(true))
                .andReturn();

        System.out.println("\n[RESPONSE 2.1] Future Dynamic Slots:\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 2.2: Validate promo code with percentage discount & max cap (200 OK)")
    void testValidateCoupon_PercentageWithCap() throws Exception {
        CouponValidateRequestDTO request = CouponValidateRequestDTO.builder()
                .code("WELCOME50")
                .orderAmount(BigDecimal.valueOf(1499.00))
                .serviceId(1L)
                .customerId(customerId.toString())
                .build();

        CouponValidationResponseDTO response = CouponValidationResponseDTO.builder()
                .valid(true)
                .couponCode("WELCOME50")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(50.00))
                .orderAmount(BigDecimal.valueOf(1499.00))
                .discountAmount(BigDecimal.valueOf(250.00))
                .finalAmount(BigDecimal.valueOf(1249.00))
                .message("Promo code applied successfully! You saved Rs 250.00")
                .build();

        when(couponService.validateCoupon(any())).thenReturn(response);

        MvcResult result = couponMockMvc.perform(post("/api/coupons/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.discountAmount").value(250.00))
                .andExpect(jsonPath("$.finalAmount").value(1249.00))
                .andReturn();

        System.out.println("\n[RESPONSE 2.2] Validate Coupon (WELCOME50 with cap):\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 2.3: Validate coupon below minimum order amount (200 OK - valid=false)")
    void testValidateCoupon_BelowMinAmount() throws Exception {
        CouponValidateRequestDTO request = CouponValidateRequestDTO.builder()
                .code("FESTIVE100")
                .orderAmount(BigDecimal.valueOf(300.00))
                .build();

        CouponValidationResponseDTO response = CouponValidationResponseDTO.builder()
                .valid(false)
                .couponCode("FESTIVE100")
                .orderAmount(BigDecimal.valueOf(300.00))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(300.00))
                .message("Minimum order amount of Rs 500.00 required for this promo code")
                .build();

        when(couponService.validateCoupon(any())).thenReturn(response);

        MvcResult result = couponMockMvc.perform(post("/api/coupons/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Minimum order amount of Rs 500.00 required for this promo code"))
                .andReturn();

        System.out.println("\n[RESPONSE 2.3] Coupon Below Threshold:\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 2.4: Create booking - Start PIN is MASKED in response (201 Created)")
    void testCreateBooking_PinMasked() throws Exception {
        BookingCreateRequestDTO request = BookingCreateRequestDTO.builder()
                .serviceId(1L)
                .serviceName("Deep Home Cleaning")
                .categoryId(categoryId.toString())
                .bookingDate(LocalDate.of(2026, 9, 20))
                .slotId("SLOT-0912")
                .scheduledTime("09:00 AM - 12:00 PM")
                .totalAmount(BigDecimal.valueOf(1499.00))
                .addressId(addressId1.toString())
                .couponCode("WELCOME50")
                .build();

        BookingResponseDTO response = BookingResponseDTO.builder()
                .id(1L)
                .customerId(customerId.toString())
                .serviceId(1L)
                .serviceName("Deep Home Cleaning")
                .categoryId(categoryId.toString())
                .bookingDate(LocalDate.of(2026, 9, 20))
                .status(BookingStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(1499.00))
                .discountAmount(BigDecimal.valueOf(250.00))
                .payableAmount(BigDecimal.valueOf(1249.00))
                .addressSummary("Flat 402, Lotus Heights, Madhapur, Hyderabad")
                .slotId("SLOT-0912")
                .scheduledTime("09:00 AM - 12:00 PM")
                .couponCode("WELCOME50")
                .startPin(null) // STRICT SECURITY: PIN is masked/null
                .startPinVerified(false)
                .pinExpiresAt(LocalDateTime.of(2026, 9, 20, 23, 59, 59))
                .build();

        when(bookingService.createBooking(any(), eq(customerId.toString()))).thenReturn(response);

        MvcResult result = bookingMockMvc.perform(post("/api/bookings")
                        .header("X-Customer-Id", customerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.payableAmount").value(1249.00))
                .andExpect(jsonPath("$.startPin").doesNotExist()) // Masked
                .andReturn();

        System.out.println("\n[RESPONSE 2.4] Create Booking (PIN Masked):\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 2.5: View Booking when status is PENDING/CONFIRMED - PIN is MASKED (200 OK)")
    void testGetBooking_Pending_PinMasked() throws Exception {
        BookingResponseDTO response = BookingResponseDTO.builder()
                .id(1L)
                .customerId(customerId.toString())
                .status(BookingStatus.CONFIRMED)
                .startPin(null) // Masked
                .build();

        when(bookingService.getBookingById(1L, customerId.toString())).thenReturn(response);

        MvcResult result = bookingMockMvc.perform(get("/api/bookings/1")
                        .header("X-Customer-Id", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.startPin").doesNotExist())
                .andReturn();

        System.out.println("\n[RESPONSE 2.5] View Booking in CONFIRMED (PIN Masked):\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 2.6: View Booking when status is ARRIVED - PIN is REVEALED! (200 OK)")
    void testGetBooking_Arrived_PinRevealed() throws Exception {
        BookingResponseDTO response = BookingResponseDTO.builder()
                .id(1L)
                .customerId(customerId.toString())
                .workerId(workerId.toString())
                .status(BookingStatus.ARRIVED)
                .startPin("4589") // REVEALED when ARRIVED
                .build();

        when(bookingService.getBookingById(1L, customerId.toString())).thenReturn(response);

        MvcResult result = bookingMockMvc.perform(get("/api/bookings/1")
                        .header("X-Customer-Id", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARRIVED"))
                .andExpect(jsonPath("$.startPin").value("4589"))
                .andReturn();

        System.out.println("\n[RESPONSE 2.6] View Booking in ARRIVED (PIN Revealed: 4589):\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 2.7: Worker verifies 4-digit PIN - Starts Service (IN_PROGRESS) (200 OK)")
    void testVerifyPin_Success() throws Exception {
        VerifyPinRequestDTO request = VerifyPinRequestDTO.builder().pin("4589").build();

        BookingResponseDTO response = BookingResponseDTO.builder()
                .id(1L)
                .status(BookingStatus.IN_PROGRESS)
                .startPinVerified(true)
                .serviceStartedAt(LocalDateTime.now())
                .build();

        when(bookingService.verifyPin(1L, "4589")).thenReturn(response);

        MvcResult result = bookingMockMvc.perform(post("/api/bookings/1/verify-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.startPinVerified").value(true))
                .andReturn();

        System.out.println("\n[RESPONSE 2.7] Verify PIN (Transitions to IN_PROGRESS):\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 2.8: Worker inputs wrong PIN - Rejects with InvalidPinException (400 Bad Request)")
    void testVerifyPin_WrongPin() throws Exception {
        VerifyPinRequestDTO request = VerifyPinRequestDTO.builder().pin("9999").build();

        when(bookingService.verifyPin(1L, "9999"))
                .thenThrow(new InvalidPinException("Invalid start PIN. Attempts remaining: 4"));

        MvcResult result = bookingMockMvc.perform(post("/api/bookings/1/verify-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid PIN"))
                .andExpect(jsonPath("$.message").value("Invalid start PIN. Attempts remaining: 4"))
                .andReturn();

        System.out.println("\n[RESPONSE 2.8] Wrong PIN Error:\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 2.9: Cancel booking when PENDING - Full refund & Rs 0 fee (200 OK)")
    void testCancelBooking_Pending_FullRefund() throws Exception {
        BookingCancelRequestDTO request = BookingCancelRequestDTO.builder()
                .reason("Change of plans")
                .comments("Please refund to bank account")
                .build();

        CancelBookingResponseDTO response = CancelBookingResponseDTO.builder()
                .bookingId(1L)
                .status(BookingStatus.CANCELLED)
                .cancellationReason("Change of plans - Please refund to bank account")
                .cancellationFee(BigDecimal.ZERO)
                .refundAmount(BigDecimal.valueOf(1249.00))
                .cancelledAt(LocalDateTime.now())
                .message("Booking cancelled successfully. Eligible refund: Rs 1249.00")
                .build();

        when(bookingService.cancelBooking(eq(1L), any(), eq(customerId.toString()))).thenReturn(response);

        MvcResult result = bookingMockMvc.perform(post("/api/bookings/1/cancel")
                        .header("X-Customer-Id", customerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationFee").value(0))
                .andExpect(jsonPath("$.refundAmount").value(1249.00))
                .andReturn();

        System.out.println("\n[RESPONSE 2.9] Cancel Booking (PENDING - Full Refund):\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 2.10: Cancel booking when WORKER_ON_THE_WAY - Rs 50 penalty applied (200 OK)")
    void testCancelBooking_WorkerOnWay_FeeDeducted() throws Exception {
        BookingCancelRequestDTO request = BookingCancelRequestDTO.builder()
                .reason("Emergency arose")
                .build();

        CancelBookingResponseDTO response = CancelBookingResponseDTO.builder()
                .bookingId(1L)
                .status(BookingStatus.CANCELLED)
                .cancellationReason("Emergency arose")
                .cancellationFee(BigDecimal.valueOf(50.00))
                .refundAmount(BigDecimal.valueOf(1199.00))
                .cancelledAt(LocalDateTime.now())
                .message("Booking cancelled successfully. Eligible refund: Rs 1199.00")
                .build();

        when(bookingService.cancelBooking(eq(1L), any(), eq(customerId.toString()))).thenReturn(response);

        MvcResult result = bookingMockMvc.perform(post("/api/bookings/1/cancel")
                        .header("X-Customer-Id", customerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationFee").value(50.00))
                .andExpect(jsonPath("$.refundAmount").value(1199.00))
                .andReturn();

        System.out.println("\n[RESPONSE 2.10] Cancel Booking (WORKER_ON_THE_WAY - Penalty Deducted):\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 2.11: Cancel booking when IN_PROGRESS - Strictly rejected (400 Bad Request)")
    void testCancelBooking_InProgress_Rejected() throws Exception {
        BookingCancelRequestDTO request = BookingCancelRequestDTO.builder().reason("Stop work").build();

        when(bookingService.cancelBooking(eq(1L), any(), eq(customerId.toString())))
                .thenThrow(new InvalidBookingStateException("Cannot cancel a booking that is already IN_PROGRESS"));

        MvcResult result = bookingMockMvc.perform(post("/api/bookings/1/cancel")
                        .header("X-Customer-Id", customerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Booking State"))
                .andExpect(jsonPath("$.message").value("Cannot cancel a booking that is already IN_PROGRESS"))
                .andReturn();

        System.out.println("\n[RESPONSE 2.11] Cancel Rejection Error:\n" + result.getResponse().getContentAsString());
    }

    // ==========================================
    // DOMAIN 3: WORKER DISCOVERY & CATALOG EDGE CASES
    // ==========================================

    @Test
    @DisplayName("Edge Case 3.1: Browse active services via Admin Catalog API (200 OK)")
    void testGetServices() throws Exception {
        com.salaryneeds.dto.catalog.ServiceResponseDTO service = com.salaryneeds.dto.catalog.ServiceResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("cleaning")
                .status("ACTIVE")
                .build();

        when(catalogManagementService.getAllServices(isNull(), eq("ACTIVE"))).thenReturn(List.of(service));

        MvcResult result = catalogMockMvc.perform(get("/admin/services").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("cleaning"))
                .andReturn();

        System.out.println("\n[RESPONSE 3.1] Admin Catalog Services:\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 3.2: Search workers filtered by pincode, rating & duty (200 OK)")
    void testSearchWorkers() throws Exception {
        WorkerProfileDTO worker = WorkerProfileDTO.builder()
                .id(workerId)
                .name("Ramesh Sharma")
                .service("Deep Home Cleaning")
                .pincode("500081")
                .ratingAvg(BigDecimal.valueOf(4.95))
                .dutyOnline(true)
                .build();

        PageResponseDTO<WorkerProfileDTO> pageResponse = PageResponseDTO.<WorkerProfileDTO>builder()
                .content(List.of(worker))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(workerDiscoveryService.searchWorkers(any(), any(), eq("500081"), any(), eq(true), any()))
                .thenReturn(pageResponse);

        MvcResult result = workerMockMvc.perform(get("/api/workers/search?pincode=500081&minRating=4.5&dutyOnline=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Ramesh Sharma"))
                .andExpect(jsonPath("$.content[0].ratingAvg").value(4.95))
                .andReturn();

        System.out.println("\n[RESPONSE 3.2] Worker Search Results:\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 3.3: Get recommended workers for pincode sorted by rating (200 OK)")
    void testGetRecommendedWorkers() throws Exception {
        WorkerProfileDTO worker = WorkerProfileDTO.builder()
                .id(workerId)
                .name("Ramesh Sharma")
                .service("Deep Home Cleaning")
                .pincode("500081")
                .ratingAvg(BigDecimal.valueOf(4.95))
                .completedJobsCount(340)
                .dutyOnline(true)
                .build();

        when(workerDiscoveryService.getRecommendedWorkers("500081", categoryId, 5)).thenReturn(List.of(worker));

        MvcResult result = workerMockMvc.perform(get("/api/workers/recommended?pincode=500081&categoryId=" + categoryId + "&limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ramesh Sharma"))
                .andExpect(jsonPath("$[0].ratingAvg").value(4.95))
                .andReturn();

        System.out.println("\n[RESPONSE 3.3] Recommended Workers for Pincode:\n" + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Edge Case 3.4: Get public worker profile (200 OK)")
    void testGetWorkerProfile() throws Exception {
        WorkerProfileDTO worker = WorkerProfileDTO.builder()
                .id(workerId)
                .name("Ramesh Sharma")
                .email("ramesh.cleaner@example.com")
                .phone("9876500001")
                .service("Deep Home Cleaning")
                .skills("Deep scrubbing, Sanitization")
                .experienceYears(6)
                .pincode("500081")
                .verified(true)
                .ratingAvg(BigDecimal.valueOf(4.95))
                .completedJobsCount(340)
                .dutyOnline(true)
                .accountStatus("ACTIVE")
                .build();

        when(workerDiscoveryService.getWorkerProfile(workerId)).thenReturn(worker);

        MvcResult result = workerMockMvc.perform(get("/api/workers/" + workerId + "/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ramesh Sharma"))
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.ratingAvg").value(4.95))
                .andReturn();

        System.out.println("\n[RESPONSE 3.4] Public Worker Profile:\n" + result.getResponse().getContentAsString());
    }
}
