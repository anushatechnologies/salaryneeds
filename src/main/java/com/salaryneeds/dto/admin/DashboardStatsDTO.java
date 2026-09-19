package com.salaryneeds.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * KPI snapshot returned by GET /api/admin/dashboard/stats.
 * Field names match the stat cards shown in the admin UI.
 */
@Getter
@Builder
public class DashboardStatsDTO {

    // Workers
    private long totalWorkers;
    private long activeWorkers;

    // Customers
    private long totalCustomers;

    // Bookings
    private long totalBookings;
    private long pendingBookings;
    private long completedBookings;
    private long totalCancellations;

    // Documents
    private long pendingDocuments;

    // Reviews (formerly Testimonials)
    private long pendingReviews;

    // Revenue
    private BigDecimal totalRevenue;
}
