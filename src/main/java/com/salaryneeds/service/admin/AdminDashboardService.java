package com.salaryneeds.service.admin;

import com.salaryneeds.dto.admin.DashboardStatsDTO;
import com.salaryneeds.entity.enums.BookingStatus;
import com.salaryneeds.entity.enums.ReviewStatus;
import com.salaryneeds.repository.BookingRepository;
import com.salaryneeds.repository.CustomerRepository;
import com.salaryneeds.repository.ReviewRepository;
import com.salaryneeds.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final WorkerProfileRepository workerRepo;
    private final CustomerRepository      customerRepo;
    private final BookingRepository       bookingRepo;
    private final ReviewRepository        reviewRepo;

    public DashboardStatsDTO getStats() {

        long totalWorkers       = workerRepo.count();
        long activeWorkers      = workerRepo.countByAccountStatus("ACTIVE");
        long totalCustomers     = customerRepo.count();
        long totalBookings      = bookingRepo.count();
        long pendingBookings    = bookingRepo.countByStatus(BookingStatus.PENDING);
        long completedBookings  = bookingRepo.countByStatus(BookingStatus.COMPLETED);
        long totalCancellations = bookingRepo.countByStatus(BookingStatus.CANCELLED);
        long pendingDocuments   = workerRepo.countByVerified(false);
        long pendingReviews     = reviewRepo.countByStatus(ReviewStatus.PENDING);

        BigDecimal totalRevenue = bookingRepo.sumTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        return DashboardStatsDTO.builder()
                .totalWorkers(totalWorkers)
                .activeWorkers(activeWorkers)
                .totalCustomers(totalCustomers)
                .totalBookings(totalBookings)
                .pendingBookings(pendingBookings)
                .completedBookings(completedBookings)
                .totalCancellations(totalCancellations)
                .pendingDocuments(pendingDocuments)
                .pendingReviews(pendingReviews)
                .totalRevenue(totalRevenue)
                .build();
    }
}
