package com.salaryneeds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaryneeds.dto.*;
import com.salaryneeds.entity.enums.DocType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.flyway.enabled=false"
})
class SalaryNeedsApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.salaryneeds.repository.WorkerProfileRepository workerProfileRepository;
    @Autowired
    private com.salaryneeds.repository.WorkerDocumentRepository workerDocumentRepository;
    @Autowired
    private com.salaryneeds.repository.BookingRepository bookingRepository;
    @Autowired
    private com.salaryneeds.repository.WorkerWalletRepository workerWalletRepository;

    @BeforeEach
    void cleanDb() {
        bookingRepository.deleteAll();
        workerDocumentRepository.deleteAll();
        workerProfileRepository.deleteAll();
        workerWalletRepository.deleteAll();
    }

    // =========================================================================
    // 1. END-TO-END GOLDEN PATH FLOW
    // =========================================================================
    @Test
    @DisplayName("End-to-End Partner Journey: Register -> Login -> Duty -> Dispatch -> Complete -> Wallet -> KYC")
    void testCompletePartnerOperationsFlow() throws Exception {
        // Step 1: Register Partner
        WorkerRegisterRequest regRequest = WorkerRegisterRequest.builder()
                .name("Rajesh Sharma")
                .phone("9876543210")
                .email("rajesh.sharma@example.com")
                .trade("AC Technician")
                .categoryId("cat-ac-hvac")
                .categoryName("AC & HVAC")
                .subCategoryId("sub-split-ac")
                .subCategoryName("Split AC Servicing")
                .experienceYears(5)
                .pincode("500072")
                .city("Hyderabad")
                .address("Flat 304, Green Heights, Kukatpally")
                .serviceAreas(List.of("500072", "500081", "500084"))
                .skills(List.of("Split AC Repair", "Gas Refill", "PCB Diagnostic"))
                .build();

        mockMvc.perform(post("/worker/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.phone", is("9876543210")))
                .andExpect(jsonPath("$.data.account_status", is("PENDING_APPROVAL")));

        // Step 2: Send OTP & Login
        SendOtpRequest otpReq = new SendOtpRequest("9876543210");
        MvcResult otpResult = mockMvc.perform(post("/worker/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.otp", notNullValue()))
                .andReturn();

        String otp = objectMapper.readTree(otpResult.getResponse().getContentAsString())
                .path("data").path("otp").asText();

        WorkerLoginRequest loginReq = WorkerLoginRequest.builder()
                .username("9876543210")
                .otp(otp)
                .build();

        MvcResult loginResult = mockMvc.perform(post("/worker/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.worker.id", notNullValue()))
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("token").asText();
        String workerId = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("worker").path("id").asText();

        String authHeader = "Bearer " + token;

        // Step 3: Get Profile
        mockMvc.perform(get("/worker/profile/me")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone", is("9876543210")));

        // Step 4: Toggle Duty & Location Ping
        DutyUpdateRequest dutyReq = new DutyUpdateRequest(true);
        mockMvc.perform(patch("/worker/profile/duty")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dutyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.duty_online", is(true)));

        LocationPingRequest pingReq = LocationPingRequest.builder()
                .lat(17.4933)
                .lng(78.3995)
                .speed(24.5)
                .heading(185.0)
                .activeBookingId("SNB-2505187")
                .build();

        mockMvc.perform(post("/worker/location/ping")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pingReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Step 5: Radar & Accept Lead
        mockMvc.perform(get("/worker/bookings/nearby-leads?lat=17.4933&lng=78.3995&radius_km=5")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.count", greaterThan(0)));

        mockMvc.perform(post("/worker/bookings/SNB-99231/accept")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.booking.status", is("ACCEPTED")));

        // Step 6: Step-by-Step Job Lifecycle Transitions
        mockMvc.perform(patch("/worker/bookings/SNB-99231/status")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EN_ROUTE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("EN_ROUTE")));

        mockMvc.perform(patch("/worker/bookings/SNB-99231/status")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ARRIVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ARRIVED")));

        mockMvc.perform(patch("/worker/bookings/SNB-99231/status")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        // Toggle checklist
        mockMvc.perform(post("/worker/bookings/SNB-99231/checklist/chk-power/toggle")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Add extra part
        ExtraPartRequest partReq = new ExtraPartRequest("Capacitor 45uF 440V", new BigDecimal("380.00"));
        mockMvc.perform(post("/worker/bookings/SNB-99231/extra-parts")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.total_price", is(960.00)));

        // Verify OTP and complete
        mockMvc.perform(post("/worker/bookings/SNB-99231/verify-otp")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otp\":\"6742\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.settlement.total_payout_credited", is(960.00)));

        // Step 7: Schedule & Wallet
        mockMvc.perform(get("/worker/schedule?date=today")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots", hasSize(6)));

        mockMvc.perform(get("/worker/wallet")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.earnings_revenue.balance", greaterThanOrEqualTo(960.00)));

        BankUpdateRequest bankReq = BankUpdateRequest.builder()
                .bankName("HDFC Bank")
                .accountNumber("50100234899142")
                .ifsc("HDFC0001248")
                .accountHolder("Rajesh Sharma")
                .build();

        mockMvc.perform(post("/worker/bank/update")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bankReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bank_account.account_last4", is("9142")));

        // Step 8: Document KYC
        UploadUrlRequest urlReq = UploadUrlRequest.builder()
                .docType(DocType.AADHAAR_CARD)
                .filename("aadhaar_front.jpg")
                .contentType("image/jpeg")
                .build();

        mockMvc.perform(post("/worker/documents/upload-url")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upload_url", notNullValue()));

        DocumentConfirmRequest docConfirm = DocumentConfirmRequest.builder()
                .docType(DocType.AADHAAR_CARD)
                .s3Key("workers/" + workerId + "/aadhaar_front.jpg")
                .fileSizeBytes(1420500L)
                .originalFilename("aadhaar_front.jpg")
                .build();

        mockMvc.perform(post("/worker/documents")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(docConfirm)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.document.status", is("PENDING")));

        // Step 9: Reviews & Device Tokens
        mockMvc.perform(get("/worker/reviews")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews", not(empty())));

        DeviceTokenRequest devReq = DeviceTokenRequest.builder()
                .token("ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]")
                .platform("android")
                .deviceName("OnePlus Nord 2")
                .build();

        mockMvc.perform(post("/worker/devices/register-token")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(devReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // =========================================================================
    // 2. AUTHENTICATION & REGISTRATION EDGE CASES
    // =========================================================================
    @Test
    @DisplayName("Auth Edge Cases: Missing Fields, Invalid Formats, Duplicate Phones, Bad OTPs")
    void testAuthAndRegistrationEdgeCases() throws Exception {
        String testPhone = "91" + (int)(Math.random() * 89999999 + 10000000);

        // Edge 1: Blank Name in Registration
        mockMvc.perform(post("/worker/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + testPhone + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_PAYLOAD")));

        // Edge 2: Missing Phone in Registration
        mockMvc.perform(post("/worker/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Technician\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_PAYLOAD")));

        // Edge 3: Invalid Phone Format (too short)
        mockMvc.perform(post("/worker/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Technician\",\"phone\":\"12345\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_PAYLOAD")));

        // Edge 4: Register valid partner
        mockMvc.perform(post("/worker/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Valid Partner\",\"phone\":\"" + testPhone + "\"}"))
                .andExpect(status().isCreated());

        // Edge 5: Duplicate Phone Registration -> 409 PHONE_ALREADY_EXISTS
        mockMvc.perform(post("/worker/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Another Person\",\"phone\":\"" + testPhone + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("PHONE_ALREADY_EXISTS")));

        // Edge 6: Send OTP with blank phone
        mockMvc.perform(post("/worker/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_PAYLOAD")));

        // Edge 7: Login with invalid OTP -> 400 INVALID_OTP
        mockMvc.perform(post("/worker/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + testPhone + "\",\"otp\":\"0000\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_OTP")));

        // Edge 8: Login with blank OTP -> 400 INVALID_PAYLOAD
        mockMvc.perform(post("/worker/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + testPhone + "\",\"otp\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_PAYLOAD")));
    }

    // =========================================================================
    // 3. OPEN ACCESS & CONTEXT RESOLUTION
    // =========================================================================
    @Test
    @DisplayName("Open Access: Seamless API access without requiring JWT authentication")
    void testSecurityFilterEdgeCases() throws Exception {
        // Direct access without any Bearer token returns 200 OK with default context
        mockMvc.perform(get("/worker/profile/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()));

        mockMvc.perform(get("/worker/wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Direct access with workerId header returns 200 OK
        mockMvc.perform(get("/worker/profile/me")
                        .header("X-Worker-Id", "w-default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()));

        // Public and admin endpoints continue returning 200 OK
        mockMvc.perform(get("/catalog/categories"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/workers/documents"))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // 4. BOOKING STATE MACHINE & DISPATCH EDGE CASES
    // =========================================================================
    @Test
    @DisplayName("Booking Edge Cases: Non-Existent ID, Illegal State Transitions, OTP Verification, Double Claim")
    void testBookingStateMachineAndTransitionEdgeCases() throws Exception {
        String phoneWorker1 = "92" + (int)(Math.random() * 89999999 + 10000000);
        String phoneWorker2 = "93" + (int)(Math.random() * 89999999 + 10000000);

        // Register Worker 1
        mockMvc.perform(post("/worker/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Worker One\",\"phone\":\"" + phoneWorker1 + "\"}"))
                .andExpect(status().isCreated());

        MvcResult login1 = mockMvc.perform(post("/worker/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + phoneWorker1 + "\",\"otp\":\"4829\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String token1 = objectMapper.readTree(login1.getResponse().getContentAsString()).path("token").asText();
        String authHeader1 = "Bearer " + token1;

        // Register Worker 2
        mockMvc.perform(post("/worker/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Worker Two\",\"phone\":\"" + phoneWorker2 + "\"}"))
                .andExpect(status().isCreated());

        MvcResult login2 = mockMvc.perform(post("/worker/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + phoneWorker2 + "\",\"otp\":\"4829\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String token2 = objectMapper.readTree(login2.getResponse().getContentAsString()).path("token").asText();
        String authHeader2 = "Bearer " + token2;

        // Edge 1: Query Non-Existent Booking -> 404 BOOKING_NOT_FOUND
        mockMvc.perform(get("/worker/bookings/SNB-NON-EXISTENT-999")
                        .header("Authorization", authHeader1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("BOOKING_NOT_FOUND")));

        // Edge 2: Update status of non-existent booking -> 404 BOOKING_NOT_FOUND
        mockMvc.perform(patch("/worker/bookings/SNB-NON-EXISTENT-999/status")
                        .header("Authorization", authHeader1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EN_ROUTE\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("BOOKING_NOT_FOUND")));

        // Claim a unique booking by Worker 1
        String bookingId = "SNB-TEST-" + System.currentTimeMillis();
        mockMvc.perform(post("/worker/bookings/" + bookingId + "/accept")
                        .header("Authorization", authHeader1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booking.status", is("ACCEPTED")));

        // Edge 3: Worker 2 attempting to claim the same booking -> 409 LEAD_ALREADY_CLAIMED
        mockMvc.perform(post("/worker/bookings/" + bookingId + "/accept")
                        .header("Authorization", authHeader2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("LEAD_ALREADY_CLAIMED")));

        // Edge 4: Invalid Status Name string -> 422 UNPROCESSABLE_STATUS
        mockMvc.perform(patch("/worker/bookings/" + bookingId + "/status")
                        .header("Authorization", authHeader1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVALID_STATUS_STRING\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error", is("UNPROCESSABLE_STATUS")));

        // Edge 5: Direct illegal transition from ACCEPTED to COMPLETED via PATCH -> 422 UNPROCESSABLE_STATUS
        mockMvc.perform(patch("/worker/bookings/" + bookingId + "/status")
                        .header("Authorization", authHeader1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error", is("UNPROCESSABLE_STATUS")));

        // Edge 6: Direct illegal skip from ACCEPTED to IN_PROGRESS (must be EN_ROUTE first) -> 422 UNPROCESSABLE_STATUS
        mockMvc.perform(patch("/worker/bookings/" + bookingId + "/status")
                        .header("Authorization", authHeader1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error", is("UNPROCESSABLE_STATUS")));

        // Valid transition: ACCEPTED -> EN_ROUTE
        mockMvc.perform(patch("/worker/bookings/" + bookingId + "/status")
                        .header("Authorization", authHeader1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EN_ROUTE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("EN_ROUTE")));

        // Valid transition: EN_ROUTE -> ARRIVED
        mockMvc.perform(patch("/worker/bookings/" + bookingId + "/status")
                        .header("Authorization", authHeader1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ARRIVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ARRIVED")));

        // Valid transition: ARRIVED -> IN_PROGRESS
        mockMvc.perform(patch("/worker/bookings/" + bookingId + "/status")
                        .header("Authorization", authHeader1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        // Edge 7: Add extra part with blank name -> 400 INVALID_PAYLOAD
        mockMvc.perform(post("/worker/bookings/" + bookingId + "/extra-parts")
                        .header("Authorization", authHeader1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\", \"price\": 200.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_PAYLOAD")));

        // Edge 8: Add extra part with negative price -> 400 INVALID_PAYLOAD
        mockMvc.perform(post("/worker/bookings/" + bookingId + "/extra-parts")
                        .header("Authorization", authHeader1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Filter\", \"price\": -50.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_PAYLOAD")));

        // Edge 9: Verify OTP with incorrect 4-digit code -> 400 INVALID_OTP
        mockMvc.perform(post("/worker/bookings/" + bookingId + "/verify-otp")
                        .header("Authorization", authHeader1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otp\":\"1111\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_OTP")));

        // Valid OTP verification marks COMPLETED
        mockMvc.perform(post("/worker/bookings/" + bookingId + "/verify-otp")
                        .header("Authorization", authHeader1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otp\":\"6742\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        // Edge 10: Re-verifying OTP on already COMPLETED booking -> 422 UNPROCESSABLE_STATUS
        mockMvc.perform(post("/worker/bookings/" + bookingId + "/verify-otp")
                        .header("Authorization", authHeader1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otp\":\"6742\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error", is("UNPROCESSABLE_STATUS")));

        // Edge 11: Attempting to transition a COMPLETED booking -> 422 UNPROCESSABLE_STATUS
        mockMvc.perform(patch("/worker/bookings/" + bookingId + "/status")
                        .header("Authorization", authHeader1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EN_ROUTE\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error", is("UNPROCESSABLE_STATUS")));
    }

    // =========================================================================
    // 5. WALLET & BANKING EDGE CASES
    // =========================================================================
    @Test
    @DisplayName("Wallet & Bank Edge Cases: Insufficient Withdrawal Funds, Invalid Bank Fields")
    void testWalletAndBankingEdgeCases() throws Exception {
        String phone = "94" + (int)(Math.random() * 89999999 + 10000000);
        mockMvc.perform(post("/worker/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bank Test User\",\"phone\":\"" + phone + "\"}"))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/worker/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + phone + "\",\"otp\":\"4829\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).path("token").asText();
        String authHeader = "Bearer " + token;

        // Edge 1: Bank update with missing bankName -> 400 INVALID_PAYLOAD
        mockMvc.perform(post("/worker/bank/update")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account_number\":\"1234567890\",\"ifsc\":\"HDFC0001234\",\"account_holder\":\"User\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_PAYLOAD")));

        // Edge 2: Bank update with missing IFSC -> 400 INVALID_PAYLOAD
        mockMvc.perform(post("/worker/bank/update")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bank_name\":\"SBI\",\"account_number\":\"1234567890\",\"account_holder\":\"User\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_PAYLOAD")));

        // Edge 3: Withdrawal exceeding wallet earnings balance (wallet is currently 0) -> 400 BAD_REQUEST
        WithdrawRequest excessWithdraw = WithdrawRequest.builder()
                .amount(new BigDecimal("999999.00"))
                .upiId("user@upi")
                .payoutMode("UPI")
                .build();
        mockMvc.perform(post("/worker/wallet/withdraw")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(excessWithdraw)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("ERR_INSUFFICIENT_FUNDS")));

        // Fund the wallet with 1000.00 to test successful withdrawal
        com.salaryneeds.entity.WorkerWallet wallet = workerWalletRepository.findByWorkerId(token).orElseThrow();
        wallet.setEarningsBalance(new BigDecimal("1000.00"));
        workerWalletRepository.save(wallet);

        // Valid withdrawal within limits
        WithdrawRequest validWithdraw = WithdrawRequest.builder()
                .amount(new BigDecimal("500.00"))
                .upiId("user@upi")
                .payoutMode("UPI")
                .build();
        mockMvc.perform(post("/worker/wallet/withdraw")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validWithdraw)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is("SUCCESS")));
    }

    // =========================================================================
    // 6. ADMIN DOCUMENT VERIFICATION & KYC FLOW
    // =========================================================================
    @Test
    @DisplayName("Admin Document Management: List, View, Approve, Reject")
    void testAdminAndWorkerEndpoints() throws Exception {
        // 1. List Worker Documents (GET /admin/workers/documents)
        mockMvc.perform(get("/admin/workers/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].document_type", is("AADHAAR_CARD")))
                .andExpect(jsonPath("$[0].document_url", notNullValue()));

        // 2. Get Document by ID (GET /admin/workers/documents/1)
        mockMvc.perform(get("/admin/workers/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.worker_id", is(1)))
                .andExpect(jsonPath("$.document_type", is("AADHAAR_CARD")))
                .andExpect(jsonPath("$.document_url", notNullValue()));

        // 3. Approve Document (POST /admin/workers/documents/1/approve)
        mockMvc.perform(post("/admin/workers/documents/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.reviewed_at", notNullValue()));

        // 4. Reject Document (POST /admin/workers/documents/1/reject)
        mockMvc.perform(post("/admin/workers/documents/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.reviewed_at", notNullValue()));
    }

    // =========================================================================
    // 7. CUSTOMER, WORKER & ADMIN REVIEW OPERATIONS
    // =========================================================================
    @Test
    @DisplayName("Review Operations: Submit, Worker Summary, Worker List, Admin List, Admin Summary, Delete Review")
    void testCustomerAndAdminReviewOperations() throws Exception {
        String testWorkerId = "w-rev-test-" + System.currentTimeMillis();

        // 1. Submit a Customer Review (POST /api/customer/reviews)
        String reviewPayload = "{"
                + "\"worker_id\":\"" + testWorkerId + "\","
                + "\"booking_id\":\"SNB-TEST-99\","
                + "\"customer_name\":\"Aarav Mehta\","
                + "\"service_title\":\"AC Filter Cleaning\","
                + "\"rating\":5.0,"
                + "\"comment\":\"Outstanding and quick service!\""
                + "}";

        MvcResult submitResult = mockMvc.perform(post("/api/customer/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.review.id", notNullValue()))
                .andExpect(jsonPath("$.review.rating", is(5.0)))
                .andReturn();

        String reviewId = objectMapper.readTree(submitResult.getResponse().getContentAsString()).path("review").path("id").asText();

        // 2. Get Worker Rating Summary (GET /api/workers/{workerId}/reviews/summary)
        mockMvc.perform(get("/api/workers/" + testWorkerId + "/reviews/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.total_reviews", is(1)))
                .andExpect(jsonPath("$.average_rating", is(5.0)));

        // 3. Get Worker Reviews List (GET /api/workers/{workerId}/reviews)
        mockMvc.perform(get("/api/workers/" + testWorkerId + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.reviews", hasSize(1)))
                .andExpect(jsonPath("$.reviews[0].id", is(reviewId)))
                .andExpect(jsonPath("$.reviews[0].customer_name", is("Aarav Mehta")));

        // 4. Admin List Reviews for Worker (GET /admin/workers/{workerId}/reviews)
        mockMvc.perform(get("/admin/workers/" + testWorkerId + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.reviews", hasSize(1)))
                .andExpect(jsonPath("$.reviews[0].id", is(reviewId)));

        // 5. Admin Rating Card Summary (GET /admin/workers/{workerId}/reviews/summary)
        mockMvc.perform(get("/admin/workers/" + testWorkerId + "/reviews/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.total_reviews", is(1)))
                .andExpect(jsonPath("$.average_rating", is(5.0)));

        // 6. Delete Review by Admin (DELETE /admin/reviews/{id})
        mockMvc.perform(delete("/admin/reviews/" + reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.deleted_id", is(reviewId)));

        // Edge Case: Delete non-existent review -> 404 REVIEW_NOT_FOUND
        mockMvc.perform(delete("/admin/reviews/non-existent-rev-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("REVIEW_NOT_FOUND")));
    }
}
