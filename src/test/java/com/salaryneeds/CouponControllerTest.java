package com.salaryneeds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaryneeds.controller.CouponController;
import com.salaryneeds.dto.*;
import com.salaryneeds.entity.enums.DiscountType;
import com.salaryneeds.exception.BookingNotFoundException;
import com.salaryneeds.exception.CouponNotFoundException;
import com.salaryneeds.exception.GlobalExceptionHandler;
import com.salaryneeds.service.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {CouponController.class})
@Import({GlobalExceptionHandler.class})
public class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CouponService couponService;

    // 1. POST /api/coupons/validate — Previews discount for draft booking without writing to DB
    @Test
    @DisplayName("POST /api/coupons/validate - Valid coupon preview")
    void testValidateCoupon_Success() throws Exception {
        CouponValidateRequestDTO request = CouponValidateRequestDTO.builder()
                .code("WELCOME50")
                .orderAmount(BigDecimal.valueOf(1000.00))
                .build();

        CouponValidationResponseDTO response = CouponValidationResponseDTO.builder()
                .valid(true)
                .couponCode("WELCOME50")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(50.00))
                .originalPrice(BigDecimal.valueOf(1000.00))
                .discountApplied(BigDecimal.valueOf(250.00))
                .finalPayablePrice(BigDecimal.valueOf(750.00))
                .message("Promo code applied successfully! You saved Rs 250.00")
                .build();

        when(couponService.validateCoupon(any(CouponValidateRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/coupons/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.coupon_code").value("WELCOME50"))
                .andExpect(jsonPath("$.discount_applied").value(250.00))
                .andExpect(jsonPath("$.final_payable_price").value(750.00));
    }

    // 2. POST /api/bookings/{id}/apply-coupon — Validates, saves redemption & updates booking price in 1 transaction
    @Test
    @DisplayName("POST /api/bookings/{id}/apply-coupon - Apply coupon successfully")
    void testApplyCoupon_Success() throws Exception {
        ApplyCouponRequestDTO request = ApplyCouponRequestDTO.builder()
                .code("FLAT100")
                .customerId("cust-123")
                .build();

        ApplyCouponResponseDTO response = ApplyCouponResponseDTO.builder()
                .bookingId(100L)
                .couponCode("FLAT100")
                .originalPrice(BigDecimal.valueOf(500.00))
                .discountApplied(BigDecimal.valueOf(100.00))
                .finalPayablePrice(BigDecimal.valueOf(400.00))
                .message("Coupon applied successfully")
                .build();

        when(couponService.applyCouponToBooking(eq(100L), eq("FLAT100"), eq("cust-123"))).thenReturn(response);

        mockMvc.perform(post("/api/bookings/100/apply-coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Customer-Id", "cust-123")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booking_id").value(100))
                .andExpect(jsonPath("$.coupon_code").value("FLAT100"))
                .andExpect(jsonPath("$.discount_applied").value(100.00))
                .andExpect(jsonPath("$.final_payable_price").value(400.00));
    }

    // 3. DELETE /api/bookings/{id}/coupon — Removes coupon before payment and restores original price
    @Test
    @DisplayName("DELETE /api/bookings/{id}/coupon - Remove coupon successfully")
    void testRemoveCoupon_Success() throws Exception {
        BookingResponseDTO response = BookingResponseDTO.builder()
                .id(100L)
                .totalAmount(BigDecimal.valueOf(500.00))
                .discountAmount(BigDecimal.ZERO)
                .payableAmount(BigDecimal.valueOf(500.00))
                .couponCode(null)
                .build();

        when(couponService.removeCouponFromBooking(100L)).thenReturn(response);

        mockMvc.perform(delete("/api/bookings/100/coupon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.discountAmount").value(0))
                .andExpect(jsonPath("$.payableAmount").value(500.00));
    }

    // 4. POST /api/admin/coupons — Creates a coupon with its discount rules
    @Test
    @DisplayName("POST /api/admin/coupons - Create coupon successfully")
    void testCreateCoupon_Success() throws Exception {
        CouponDTO dto = CouponDTO.builder()
                .code("NEWYEAR50")
                .discountType(DiscountType.FLAT)
                .discountValue(BigDecimal.valueOf(50.00))
                .active(true)
                .build();

        CouponDTO response = CouponDTO.builder()
                .id(1L)
                .code("NEWYEAR50")
                .discountType(DiscountType.FLAT)
                .discountValue(BigDecimal.valueOf(50.00))
                .active(true)
                .build();

        when(couponService.createCoupon(any(CouponDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/coupons")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("NEWYEAR50"));
    }

    // 5. GET /api/admin/coupons — Lists coupons, with filters and pagination
    @Test
    @DisplayName("GET /api/admin/coupons - Paginated and filtered coupon list")
    void testGetAllCoupons_Paginated() throws Exception {
        CouponDTO dto = CouponDTO.builder().id(1L).code("CODE1").active(true).build();
        PageResponseDTO<CouponDTO> pageResponse = PageResponseDTO.<CouponDTO>builder()
                .content(List.of(dto))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .isFirst(true)
                .isLast(true)
                .build();

        when(couponService.getAllCoupons(eq(true), eq("CODE1"), any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/admin/coupons")
                        .header("X-Role", "ADMIN")
                        .param("active", "true")
                        .param("code", "CODE1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("CODE1"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // 6. GET /api/admin/coupons/{id} — Returns details of one coupon
    @Test
    @DisplayName("GET /api/admin/coupons/{id} - Get single coupon details")
    void testGetCouponById_Success() throws Exception {
        CouponDTO response = CouponDTO.builder().id(1L).code("CODE1").active(true).build();
        when(couponService.getCouponById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/admin/coupons/1")
                        .header("X-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("CODE1"));
    }

    // 7. PATCH /api/admin/coupons/{id} — Edits coupon's rules or toggles active (kill-switch)
    @Test
    @DisplayName("PATCH /api/admin/coupons/{id} - Patch coupon / toggle active kill-switch")
    void testPatchCoupon_ToggleActive() throws Exception {
        CouponDTO patchDto = CouponDTO.builder().active(false).build();
        CouponDTO response = CouponDTO.builder().id(1L).code("CODE1").active(false).build();

        when(couponService.patchCoupon(eq(1L), any(CouponDTO.class))).thenReturn(response);

        mockMvc.perform(patch("/api/admin/coupons/1")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.active").value(false));
    }

    // 8. GET /api/admin/coupons/{id}/redemptions — Returns redemption log and usage count for one coupon
    @Test
    @DisplayName("GET /api/admin/coupons/{id}/redemptions - Returns redemption log & usage count")
    void testGetCouponRedemptions_Success() throws Exception {
        CouponRedemptionDTO redemptionDTO = CouponRedemptionDTO.builder()
                .id(UUID.randomUUID())
                .couponId(1L)
                .customerId("cust-1")
                .bookingId(100L)
                .discountApplied(BigDecimal.valueOf(50.00))
                .redeemedAt(LocalDateTime.now())
                .build();

        PageResponseDTO<CouponRedemptionDTO> page = PageResponseDTO.<CouponRedemptionDTO>builder()
                .content(List.of(redemptionDTO))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        CouponRedemptionsResponseDTO response = CouponRedemptionsResponseDTO.builder()
                .couponId(1L)
                .couponCode("CODE1")
                .usageCount(5)
                .usageLimit(100)
                .redemptions(page)
                .build();

        when(couponService.getCouponRedemptions(eq(1L), any(Pageable.class))).thenReturn(response);

        mockMvc.perform(get("/api/admin/coupons/1/redemptions")
                        .header("X-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coupon_id").value(1))
                .andExpect(jsonPath("$.coupon_code").value("CODE1"))
                .andExpect(jsonPath("$.usage_count").value(5))
                .andExpect(jsonPath("$.redemptions.content[0].booking_id").value(100));
    }

    @Test
    @DisplayName("GET /api/coupons - User panel lists only available/accepted coupons")
    void testGetAvailableCoupons_Success() throws Exception {
        CouponDTO dto = CouponDTO.builder().id(1L).code("ACTIVE1").active(true).build();
        when(couponService.getAvailableCoupons()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("ACTIVE1"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0].isAccepted").value(true));
    }

    @Test
    @DisplayName("PATCH /api/admin/coupons/{id}/activate - Admin activates/accepts coupon")
    void testActivateCoupon_Success() throws Exception {
        CouponDTO response = CouponDTO.builder().id(1L).code("CODE1").active(true).build();
        when(couponService.activateCoupon(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/admin/coupons/1/activate")
                        .header("X-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.isAccepted").value(true));
    }

    @Test
    @DisplayName("PATCH /api/admin/coupons/{id}/deactivate - Admin deactivates coupon")
    void testDeactivateCoupon_Success() throws Exception {
        CouponDTO response = CouponDTO.builder().id(1L).code("CODE1").active(false).build();
        when(couponService.deactivateCoupon(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/admin/coupons/1/deactivate")
                        .header("X-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.isAccepted").value(false));
    }

    @Test
    @DisplayName("POST /api/admin/coupons/{id}/accept - Admin accepts coupon")
    void testAcceptCoupon_Success() throws Exception {
        CouponDTO response = CouponDTO.builder().id(1L).code("CODE1").active(true).build();
        when(couponService.activateCoupon(1L)).thenReturn(response);

        mockMvc.perform(post("/api/admin/coupons/1/accept")
                        .header("X-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.active").value(true));
    }
}
