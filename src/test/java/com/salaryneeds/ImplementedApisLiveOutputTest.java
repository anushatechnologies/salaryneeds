package com.salaryneeds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaryneeds.controller.CouponController;
import com.salaryneeds.controller.WorkerJobApiController;
import com.salaryneeds.dto.CouponDTO;
import com.salaryneeds.dto.LocationHeartbeatRequest;
import com.salaryneeds.entity.enums.DiscountType;
import com.salaryneeds.exception.GlobalExceptionHandler;
import com.salaryneeds.repository.BookingOfferRepository;
import com.salaryneeds.repository.BookingRepository;
import com.salaryneeds.service.BookingOfferService;
import com.salaryneeds.service.CouponService;
import com.salaryneeds.service.DutyLocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {WorkerJobApiController.class, CouponController.class})
@Import({GlobalExceptionHandler.class})
public class ImplementedApisLiveOutputTest {

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

    @MockBean
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        when(bookingRepository.findAll()).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("Run and Print All Implemented APIs")
    void testAllImplementedApis() throws Exception {
        System.out.println("\n================================================================================");
        System.out.println("                 TESTING ALL IMPLEMENTED APIS (LOCAL APPLICATION)               ");
        System.out.println("================================================================================");

        // --- 1. Worker Duty Status ---
        when(dutyLocationService.toggleDuty(eq("w-101"), eq(true)))
                .thenReturn(Map.of("dutyOnline", true, "message", "You are now online and available for bookings"));
        MvcResult res1 = mockMvc.perform(patch("/api/workers/w-101/duty-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"duty_status\": \"ON_DUTY\"}"))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("\n[API 1] PATCH /api/workers/{workerId}/duty-status");
        System.out.println("Request : {\"duty_status\": \"ON_DUTY\"}");
        System.out.println("Status  : HTTP " + res1.getResponse().getStatus() + " OK");
        System.out.println("Response: " + res1.getResponse().getContentAsString());

        // --- 2. Worker Heartbeat ---
        when(dutyLocationService.recordHeartbeat(eq("w-101"), any(LocationHeartbeatRequest.class)))
                .thenReturn(Map.of("success", true, "status", "ACK"));
        MvcResult res2 = mockMvc.perform(post("/api/workers/w-101/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lat\": 17.4933, \"lng\": 78.3995, \"speed_kmh\": 12.4, \"battery_pct\": 85, \"heading\": 90.0}"))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("\n[API 2] POST /api/workers/{workerId}/heartbeat");
        System.out.println("Request : {\"lat\": 17.4933, \"lng\": 78.3995, \"speed_kmh\": 12.4, \"battery_pct\": 85, \"heading\": 90.0}");
        System.out.println("Status  : HTTP " + res2.getResponse().getStatus() + " OK");
        System.out.println("Response: " + res2.getResponse().getContentAsString());

        // --- 3. Worker Location Update ---
        when(dutyLocationService.recordHeartbeat(eq("w-101"), any(LocationHeartbeatRequest.class)))
                .thenReturn(Map.of("success", true));
        MvcResult res3 = mockMvc.perform(patch("/api/workers/w-101/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lat\": 17.4933, \"lng\": 78.3995}"))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("\n[API 3] PATCH /api/workers/{workerId}/location");
        System.out.println("Request : {\"lat\": 17.4933, \"lng\": 78.3995}");
        System.out.println("Status  : HTTP " + res3.getResponse().getStatus() + " OK");
        System.out.println("Response: " + res3.getResponse().getContentAsString());

        // --- 4. Nearby Jobs ---
        MvcResult res4 = mockMvc.perform(get("/api/workers/w-101/nearby-jobs")
                        .param("radius", "5km"))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("\n[API 4] GET /api/workers/{workerId}/nearby-jobs?radius=5km");
        System.out.println("Status  : HTTP " + res4.getResponse().getStatus() + " OK");
        System.out.println("Response: " + res4.getResponse().getContentAsString());

        // --- 5. Get Job Details ---
        MvcResult res5 = mockMvc.perform(get("/api/jobs/SNB-99231"))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("\n[API 5] GET /api/jobs/{jobId}");
        System.out.println("Status  : HTTP " + res5.getResponse().getStatus() + " OK");
        System.out.println("Response: " + res5.getResponse().getContentAsString());

        // --- 6. Accept Job ---
        MvcResult res6 = mockMvc.perform(post("/api/jobs/SNB-99231/accept")
                        .header("X-Worker-Id", "w-101"))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("\n[API 6] POST /api/jobs/{jobId}/accept");
        System.out.println("Header  : X-Worker-Id: w-101");
        System.out.println("Status  : HTTP " + res6.getResponse().getStatus() + " OK");
        System.out.println("Response: " + res6.getResponse().getContentAsString());

        // --- 7. Reject Job ---
        MvcResult res7 = mockMvc.perform(post("/api/jobs/SNB-99231/reject")
                        .header("X-Worker-Id", "w-101"))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("\n[API 7] POST /api/jobs/{jobId}/reject");
        System.out.println("Header  : X-Worker-Id: w-101");
        System.out.println("Status  : HTTP " + res7.getResponse().getStatus() + " OK");
        System.out.println("Response: " + res7.getResponse().getContentAsString());

        // --- 8. Customer Panel Available Coupons ---
        CouponDTO coupon = CouponDTO.builder()
                .id(1L)
                .code("WELCOME50")
                .description("50% off up to Rs 250")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(50.00))
                .minBookingValue(BigDecimal.valueOf(400.00))
                .maxDiscount(BigDecimal.valueOf(250.00))
                .active(true)
                .build();
        when(couponService.getAvailableCoupons()).thenReturn(List.of(coupon));
        MvcResult res8 = mockMvc.perform(get("/api/coupons"))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("\n[API 8] GET /api/coupons (Customer Panel - Available Coupons Only)");
        System.out.println("Status  : HTTP " + res8.getResponse().getStatus() + " OK");
        System.out.println("Response: " + res8.getResponse().getContentAsString());

        // --- 9. Admin Activate Coupon ---
        CouponDTO activatedCoupon = CouponDTO.builder()
                .id(1L)
                .code("WELCOME50")
                .active(true)
                .build();
        when(couponService.activateCoupon(1L)).thenReturn(activatedCoupon);
        MvcResult res9 = mockMvc.perform(patch("/api/admin/coupons/1/activate")
                        .header("X-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("\n[API 9] PATCH /api/admin/coupons/{id}/activate (Admin Accept/Activate)");
        System.out.println("Status  : HTTP " + res9.getResponse().getStatus() + " OK");
        System.out.println("Response: " + res9.getResponse().getContentAsString());

        // --- 10. Admin Deactivate Coupon ---
        CouponDTO deactivatedCoupon = CouponDTO.builder()
                .id(1L)
                .code("WELCOME50")
                .active(false)
                .build();
        when(couponService.deactivateCoupon(1L)).thenReturn(deactivatedCoupon);
        MvcResult res10 = mockMvc.perform(patch("/api/admin/coupons/1/deactivate")
                        .header("X-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("\n[API 10] PATCH /api/admin/coupons/{id}/deactivate (Admin Deactivate)");
        System.out.println("Status  : HTTP " + res10.getResponse().getStatus() + " OK");
        System.out.println("Response: " + res10.getResponse().getContentAsString());

        System.out.println("\n================================================================================");
        System.out.println("                 ALL IMPLEMENTED APIS EXECUTED SUCCESSFULLY                     ");
        System.out.println("================================================================================\n");
    }
}
