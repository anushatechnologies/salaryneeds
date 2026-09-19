package com.salaryneeds;

import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.admin.CouponCreateRequestDTO;
import com.salaryneeds.dto.admin.CouponResponseDTO;
import com.salaryneeds.dto.admin.CouponUpdateRequestDTO;
import com.salaryneeds.entity.Coupon;
import com.salaryneeds.entity.enums.DiscountType;
import com.salaryneeds.repository.CouponRepository;
import com.salaryneeds.service.admin.AdminCouponService;
import com.salaryneeds.service.admin.AuditLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AdminCouponService adminCouponService;

    private final UUID adminId = UUID.randomUUID();

    @Test
    @DisplayName("Create coupon successfully with audit log")
    void testCreateCoupon_Success() {
        CouponCreateRequestDTO req = new CouponCreateRequestDTO();
        req.setCode("SUMMER20");
        req.setDescription("Summer discount");
        req.setDiscountType(DiscountType.PERCENTAGE);
        req.setDiscountValue(BigDecimal.valueOf(20));
        req.setMinOrderAmount(BigDecimal.valueOf(200));

        when(couponRepository.existsByCodeIgnoreCase("SUMMER20")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
            Coupon c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        CouponResponseDTO res = adminCouponService.createCoupon(req, adminId);

        assertNotNull(res);
        assertEquals("SUMMER20", res.getCode());
        assertEquals(10L, res.getId());
        verify(auditLogService, times(1)).log(eq(adminId), eq("CREATE_COUPON"), eq("COUPON"), eq("10"), anyString());
    }

    @Test
    @DisplayName("Create coupon fails when code already exists")
    void testCreateCoupon_DuplicateCode() {
        CouponCreateRequestDTO req = new CouponCreateRequestDTO();
        req.setCode("SUMMER20");

        when(couponRepository.existsByCodeIgnoreCase("SUMMER20")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> adminCouponService.createCoupon(req, adminId));
        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Toggle coupon status")
    void testToggleCoupon() {
        Coupon coupon = Coupon.builder()
                .id(5L)
                .code("DIWALI50")
                .isActive(true)
                .build();

        when(couponRepository.findById(5L)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

        CouponResponseDTO res = adminCouponService.toggleCouponStatus(5L, adminId);

        assertFalse(res.getIsActive());
        verify(auditLogService, times(1)).log(eq(adminId), eq("TOGGLE_COUPON"), eq("COUPON"), eq("5"), anyString());
    }

    @Test
    @DisplayName("Get all coupons paged")
    void testGetAllCoupons() {
        Coupon coupon = Coupon.builder()
                .id(1L)
                .code("FESTIVE100")
                .isActive(true)
                .discountType(DiscountType.FLAT)
                .discountValue(BigDecimal.valueOf(100))
                .build();

        when(couponRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(coupon)));

        PageResponseDTO<CouponResponseDTO> page = adminCouponService.getAllCoupons(null, null, PageRequest.of(0, 10));

        assertNotNull(page);
        assertEquals(1, page.getContent().size());
        assertEquals("FESTIVE100", page.getContent().get(0).getCode());
    }
}
