package com.salaryneeds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaryneeds.dto.CheckPhoneRequest;
import com.salaryneeds.dto.WorkerLoginRequest;
import com.salaryneeds.dto.WorkerSignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:workertestdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.flyway.enabled=false"
})
public class WorkerRegistrationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. Check availability of unregistered phone, aadhaar, and pan")
    void testPreRegistrationChecks() throws Exception {
        // Check phone
        CheckPhoneRequest phoneReq = new CheckPhoneRequest();
        phoneReq.setPhone("9988776655");
        mockMvc.perform(post("/api/worker/check-phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(phoneReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists", is(false)));

        // Check aadhar
        mockMvc.perform(post("/api/worker/check-aadhar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("aadharNumber", "987654321098"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists", is(false)));

        // Check pan
        mockMvc.perform(post("/api/worker/check-pan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("panNumber", "ABCDE9999Z"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists", is(false)));
    }

    @Test
    @DisplayName("2. Worker registration via JSON payload returns 201 Created and worker_id")
    void testWorkerRegistrationJson() throws Exception {
        WorkerSignupRequest signup = WorkerSignupRequest.builder()
                .name("Kiran Kumar")
                .phone("9876543219")
                .email("kiran@test.com")
                .pincode("500081")
                .city("Hyderabad")
                .address("Madhapur, Hitec City")
                .service("Electrician")
                .trade("Electrician")
                .experienceYears(4)
                .aadharNumber("987654321999")
                .panNumber("ABCDE1234K")
                .build();

        mockMvc.perform(post("/api/worker/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.worker_id", notNullValue()))
                .andExpect(jsonPath("$.message", containsString("successfully")))
                .andExpect(jsonPath("$.aadharNumber", is("987654321999")))
                .andExpect(jsonPath("$.panNumber", is("ABCDE1234K")));
    }

    @Test
    @DisplayName("3. Worker registration via Multipart Form-Data with documents returns 201 Created")
    void testWorkerRegistrationMultipart() throws Exception {
        MockMultipartFile aadharFile = new MockMultipartFile(
                "aadhar", "aadhar_card.jpg", "image/jpeg", "dummy aadhar file content".getBytes()
        );
        MockMultipartFile panFile = new MockMultipartFile(
                "pan", "pan_card.jpg", "image/jpeg", "dummy pan file content".getBytes()
        );

        mockMvc.perform(multipart("/api/worker/signup")
                        .file(aadharFile)
                        .file(panFile)
                        .param("name", "Aparna Sharma")
                        .param("phone", "9876543220")
                        .param("email", "aparna.sharma@test.com")
                        .param("pincode", "500072")
                        .param("city", "Hyderabad")
                        .param("address", "123 MG Road, Indiranagar")
                        .param("aadhar number", "123456789099")
                        .param("pan number", "ABCDE1234P")
                        .param("experience", "3"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.worker_id", notNullValue()))
                .andExpect(jsonPath("$.aadharNumber", is("123456789099")))
                .andExpect(jsonPath("$.panNumber", is("ABCDE1234P")));
    }

    @Test
    @DisplayName("4. Attempting duplicate registration with same phone returns 409 Conflict")
    void testDuplicatePhoneRegistrationFails() throws Exception {
        String existingPhone = "9876543221";

        WorkerSignupRequest first = WorkerSignupRequest.builder()
                .name("First Worker")
                .phone(existingPhone)
                .pincode("500001")
                .build();

        mockMvc.perform(post("/api/worker/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        WorkerSignupRequest duplicate = WorkerSignupRequest.builder()
                .name("Second Worker")
                .phone(existingPhone)
                .pincode("500001")
                .build();

        mockMvc.perform(post("/api/worker/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("5. Worker login flow: send-otp and login with phone & otp")
    void testWorkerLoginFlow() throws Exception {
        String phone = "9876543222";

        WorkerSignupRequest signup = WorkerSignupRequest.builder()
                .name("Raju Partner")
                .phone(phone)
                .pincode("500032")
                .city("Hyderabad")
                .build();

        mockMvc.perform(post("/api/worker/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        // Send OTP
        mockMvc.perform(post("/api/worker/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("phone", phone))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.registered", is(true)))
                .andExpect(jsonPath("$.otp", notNullValue()));

        // Login with phone and OTP
        WorkerLoginRequest loginReq = WorkerLoginRequest.builder()
                .phone(phone)
                .otp("1234")
                .build();

        mockMvc.perform(post("/api/worker/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.name", is("Raju Partner")))
                .andExpect(jsonPath("$.phone", is(phone)))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.can_access_dashboard", is(false)))
                .andExpect(jsonPath("$.is_approved", is(false)))
                .andExpect(jsonPath("$.status_message", containsString("under review")));
    }

    @Autowired
    private com.salaryneeds.repository.WorkerProfileRepository workerProfileRepository;

    @Test
    @DisplayName("6. Worker cannot access dashboard until documents approved by admin, shows message until approved")
    void testDashboardAccessRestrictedUntilAdminApproval() throws Exception {
        String phone = "9876543299";

        // 1. Worker registers
        WorkerSignupRequest signup = WorkerSignupRequest.builder()
                .name("Suresh Raina")
                .phone(phone)
                .email("suresh@example.com")
                .pincode("500072")
                .city("Hyderabad")
                .service("Plumber")
                .trade("Plumber")
                .experienceYears(3)
                .aadharNumber("987654329900")
                .panNumber("ABCDE9999K")
                .build();

        String signupJson = mockMvc.perform(post("/api/worker/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.can_access_dashboard", is(false)))
                .andExpect(jsonPath("$.is_approved", is(false)))
                .andExpect(jsonPath("$.account_status", is("PENDING_APPROVAL")))
                .andExpect(jsonPath("$.status_message", containsString("under review")))
                .andReturn().getResponse().getContentAsString();

        String workerId = objectMapper.readTree(signupJson).path("worker_id").asText();

        // 2. Check worker status via /api/worker/status endpoint before admin approval
        mockMvc.perform(get("/api/worker/" + workerId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.can_access_dashboard", is(false)))
                .andExpect(jsonPath("$.is_approved", is(false)))
                .andExpect(jsonPath("$.account_status", is("PENDING_APPROVAL")))
                .andExpect(jsonPath("$.status_message", containsString("under review")));

        // 3. Worker login before admin approval returns can_access_dashboard = false with review message
        WorkerLoginRequest loginReq = WorkerLoginRequest.builder()
                .phone(phone)
                .otp("1234")
                .build();

        mockMvc.perform(post("/api/worker/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.can_access_dashboard", is(false)))
                .andExpect(jsonPath("$.is_approved", is(false)))
                .andExpect(jsonPath("$.status_message", containsString("under review")));

        // 4. Attempting to toggle duty while pending approval is blocked (403 Forbidden)
        mockMvc.perform(patch("/worker/profile/duty")
                        .header("X-Worker-Id", workerId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("under review")));

        // 5. Admin approves worker documents
        java.util.UUID uuid = java.util.UUID.fromString(workerId);
        com.salaryneeds.entity.WorkerProfile worker = workerProfileRepository.findById(uuid).orElseThrow();
        worker.setVerified(true);
        worker.setAccountStatus(com.salaryneeds.entity.enums.AccountStatus.ACTIVE);
        workerProfileRepository.save(worker);

        // 6. Check worker status after admin approval: dashboard access granted!
        mockMvc.perform(get("/api/worker/" + workerId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.can_access_dashboard", is(true)))
                .andExpect(jsonPath("$.is_approved", is(true)))
                .andExpect(jsonPath("$.account_status", is("ACTIVE")))
                .andExpect(jsonPath("$.status_message", containsString("approved")));

        // 7. Worker login after admin approval returns can_access_dashboard = true
        mockMvc.perform(post("/api/worker/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.can_access_dashboard", is(true)))
                .andExpect(jsonPath("$.is_approved", is(true)));

        // 8. Worker profile endpoint /worker/profile/me also reflects dashboard access granted
        mockMvc.perform(get("/worker/profile/me")
                        .header("X-Worker-Id", workerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.can_access_dashboard", is(true)))
                .andExpect(jsonPath("$.is_approved", is(true)));

        // 9. Worker can now toggle duty successfully
        mockMvc.perform(patch("/worker/profile/duty")
                        .header("X-Worker-Id", workerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }
}

