package com.salaryneeds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaryneeds.controller.WorkerJobApiController;
import com.salaryneeds.dto.WorkerActionResponseDTO;
import com.salaryneeds.entity.enums.BookingStatus;
import com.salaryneeds.exception.GlobalExceptionHandler;
import com.salaryneeds.repository.BookingOfferRepository;
import com.salaryneeds.repository.BookingRepository;
import com.salaryneeds.service.BookingOfferService;
import com.salaryneeds.service.DutyLocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {WorkerJobApiController.class})
@Import({GlobalExceptionHandler.class})
public class WorkerJobApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DutyLocationService dutyLocationService;

    @MockBean
    private BookingOfferService bookingOfferService;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private BookingOfferRepository bookingOfferRepository;

    @Test
    @DisplayName("PATCH /api/workers/{workerId}/duty-status - Update duty status to ON_DUTY")
    void testUpdateDutyStatus_Online() throws Exception {
        when(dutyLocationService.toggleDuty(eq("w-123"), eq(true)))
                .thenReturn(Map.of("dutyOnline", true, "message", "You are now online"));

        Map<String, Object> req = Map.of("duty_status", "ON_DUTY");

        mockMvc.perform(patch("/api/workers/w-123/duty-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.worker_id").value("w-123"))
                .andExpect(jsonPath("$.duty_status").value("ON_DUTY"))
                .andExpect(jsonPath("$.duty_online").value(true));
    }

    @Test
    @DisplayName("PATCH /api/workers/{workerId}/duty-status - Update duty status to OFF_DUTY")
    void testUpdateDutyStatus_Offline() throws Exception {
        when(dutyLocationService.toggleDuty(eq("w-123"), eq(false)))
                .thenReturn(Map.of("dutyOnline", false, "message", "Duty status updated to Offline"));

        Map<String, Object> req = Map.of("duty_status", "OFF_DUTY");

        mockMvc.perform(patch("/api/workers/w-123/duty-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.worker_id").value("w-123"))
                .andExpect(jsonPath("$.duty_status").value("OFF_DUTY"))
                .andExpect(jsonPath("$.duty_online").value(false));
    }

    @Test
    @DisplayName("POST /api/workers/{workerId}/heartbeat - Record worker location heartbeat")
    void testHeartbeat() throws Exception {
        when(dutyLocationService.recordHeartbeat(eq("w-123"), any()))
                .thenReturn(Map.of("success", true, "status", "ACK"));

        Map<String, Object> req = Map.of(
                "lat", 17.4933,
                "lng", 78.3995,
                "speed_kmh", 15.5,
                "battery_pct", 88
        );

        mockMvc.perform(post("/api/workers/w-123/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.worker_id").value("w-123"))
                .andExpect(jsonPath("$.status").value("ACK"))
                .andExpect(jsonPath("$.message").value("Location heartbeat recorded"));
    }

    @Test
    @DisplayName("PATCH /api/workers/{workerId}/location - Update worker coordinates")
    void testUpdateLocation() throws Exception {
        when(dutyLocationService.recordHeartbeat(eq("w-123"), any()))
                .thenReturn(Map.of("success", true));

        Map<String, Object> req = Map.of(
                "lat", 17.4933,
                "lng", 78.3995
        );

        mockMvc.perform(patch("/api/workers/w-123/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.worker_id").value("w-123"))
                .andExpect(jsonPath("$.lat").value(17.4933))
                .andExpect(jsonPath("$.lng").value(78.3995));
    }

    @Test
    @DisplayName("GET /api/workers/{workerId}/nearby-jobs?radius=5km - Fetch nearby jobs")
    void testGetNearbyJobs() throws Exception {
        when(bookingRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/workers/w-123/nearby-jobs")
                        .param("radius", "5km"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.worker_id").value("w-123"))
                .andExpect(jsonPath("$.radius").value("5km"))
                .andExpect(jsonPath("$.count").isNumber())
                .andExpect(jsonPath("$.jobs").isArray());
    }

    @Test
    @DisplayName("GET /api/jobs/{jobId} - Get job details by ID")
    void testGetJobDetails_Success() throws Exception {
        mockMvc.perform(get("/api/jobs/SNB-99231"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.job.job_id").value("SNB-99231"))
                .andExpect(jsonPath("$.job.title").value("AC Split Deep Repair"));
    }

    @Test
    @DisplayName("GET /api/jobs/{jobId} - 404 for non-existent job")
    void testGetJobDetails_NotFound() throws Exception {
        mockMvc.perform(get("/api/jobs/NOT_FOUND_999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/jobs/{jobId}/accept - Accept a job")
    void testAcceptJob() throws Exception {
        when(bookingOfferRepository.existsById(anyLong())).thenReturn(false);

        mockMvc.perform(post("/api/jobs/SNB-99231/accept")
                        .header("X-Worker-Id", "w-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.job_id").value("SNB-99231"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.worker_id").value("w-123"));
    }

    @Test
    @DisplayName("POST /api/jobs/{jobId}/reject - Reject a job")
    void testRejectJob() throws Exception {
        when(bookingOfferRepository.existsById(anyLong())).thenReturn(false);

        mockMvc.perform(post("/api/jobs/SNB-99231/reject")
                        .header("X-Worker-Id", "w-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.job_id").value("SNB-99231"))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.worker_id").value("w-123"));
    }
}
