package com.salaryneeds.dto.admin;

import com.salaryneeds.entity.enums.DiscountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CouponResponseDTO {

    private Long id;
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Integer usageLimit;
    private Integer usedCount;
    private Boolean isActive;
    private Boolean isCurrentlyValid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
