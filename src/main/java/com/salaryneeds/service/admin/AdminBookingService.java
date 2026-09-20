package com.salaryneeds.service.admin;

import com.salaryneeds.dto.BookingResponseDTO;
import com.salaryneeds.dto.PageResponseDTO;
import com.salaryneeds.dto.admin.PaymentSummaryDTO;
import com.salaryneeds.entity.Booking;
import com.salaryneeds.entity.enums.BookingStatus;
import com.salaryneeds.exception.BookingNotFoundException;
import com.salaryneeds.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminBookingService {

    private final BookingRepository bookingRepo;
    private final AuditLogService   auditLogService;

    @Transactional(readOnly = true)
    public PageResponseDTO<BookingResponseDTO> getAllBookings(String statusFilter, Pageable pageable) {
        Page<Booking> page;
        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                BookingStatus status = BookingStatus.valueOf(statusFilter.trim().toUpperCase());
                page = bookingRepo.findAllByStatusOrderByCreatedAtDesc(status, pageable);
            } catch (IllegalArgumentException e) {
                page = bookingRepo.findAllByOrderByCreatedAtDesc(pageable);
            }
        } else {
            page = bookingRepo.findAllByOrderByCreatedAtDesc(pageable);
        }
        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));
        return mapToDTO(booking);
    }

    /** Admin manual status override with audit logging. */
    public BookingResponseDTO overrideStatus(Long bookingId, String status, UUID adminId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));
        BookingStatus newStatus = BookingStatus.valueOf(status.trim().toUpperCase());
        booking.setStatus(newStatus);
        bookingRepo.save(booking);

        auditLogService.log(adminId, "OVERRIDE_BOOKING_STATUS", "BOOKING",
                bookingId.toString(), "Status set to " + newStatus + " by admin");
        return mapToDTO(booking);
    }

    // ── Payments ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PaymentSummaryDTO getPaymentSummary() {
        long totalBookings     = bookingRepo.count();
        long completedBookings = bookingRepo.countByStatus(BookingStatus.COMPLETED);
        BigDecimal totalRevenue = bookingRepo.sumTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        BigDecimal totalDiscount = bookingRepo.findAllByOrderByCreatedAtDesc(Pageable.unpaged())
                .getContent().stream()
                .map(b -> b.getDiscountAmount() != null ? b.getDiscountAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PaymentSummaryDTO.builder()
                .totalBookings(totalBookings)
                .completedBookings(completedBookings)
                .totalRevenue(totalRevenue)
                .totalDiscount(totalDiscount)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<BookingResponseDTO> getPaymentDetails(Pageable pageable) {
        Page<Booking> page = bookingRepo.findAllByStatusOrderByCreatedAtDesc(
                BookingStatus.COMPLETED, pageable);
        return toPageResponse(page);
    }

    // ── Cancellation Report ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponseDTO<BookingResponseDTO> getCancellationReport(Pageable pageable) {
        Page<Booking> page = bookingRepo.findAllByStatusOrderByCreatedAtDesc(
                BookingStatus.CANCELLED, pageable);
        return toPageResponse(page);
    }

    // ── Mapping helpers ───────────────────────────────────────────────────

    private BookingResponseDTO mapToDTO(Booking b) {
        return BookingResponseDTO.builder()
                .id(b.getId())
                .customerId(b.getCustomerId())
                .workerId(b.getWorkerId())
                .serviceId(b.getServiceId())
                .serviceName(b.getServiceName())
                .categoryId(b.getCategoryId())
                .bookingDate(b.getBookingDate())
                .status(b.getStatus())
                .totalAmount(b.getTotalAmount())
                .discountAmount(b.getDiscountAmount())
                .payableAmount(b.getPayableAmount())
                .addressId(b.getAddressId())
                .addressSummary(b.getAddressSummary())
                .slotId(b.getSlotId())
                .scheduledTime(b.getScheduledTime())
                .couponCode(b.getCouponCode())
                .notes(b.getNotes())
                .cancellationReason(b.getCancellationReason())
                .cancellationFee(b.getCancellationFee())
                .refundAmount(b.getRefundAmount())
                .cancelledAt(b.getCancelledAt())
                .serviceStartedAt(b.getServiceStartedAt())
                .serviceCompletedAt(b.getServiceCompletedAt())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    private PageResponseDTO<BookingResponseDTO> toPageResponse(Page<Booking> page) {
        List<BookingResponseDTO> content = page.getContent().stream()
                .map(this::mapToDTO).collect(Collectors.toList());
        return PageResponseDTO.<BookingResponseDTO>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}
