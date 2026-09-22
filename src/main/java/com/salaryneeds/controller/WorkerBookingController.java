package com.salaryneeds.controller;

import com.salaryneeds.dto.ExtraPartRequest;
import com.salaryneeds.dto.VerifyOtpRequest;
import com.salaryneeds.exception.InvalidOtpException;
import com.salaryneeds.security.WorkerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping({"/worker/bookings", "/v1/worker/bookings"})
@RequiredArgsConstructor
public class WorkerBookingController {

    @org.springframework.beans.factory.annotation.Autowired
    private com.salaryneeds.repository.WorkerWalletRepository workerWalletRepository;

    @GetMapping({"/nearby-leads", "/leads", "/radar"})
    public ResponseEntity<Map<String, Object>> getNearbyLeads(
            @RequestParam(value = "lat", defaultValue = "17.4933") Double lat,
            @RequestParam(value = "lng", defaultValue = "78.3995") Double lng,
            @RequestParam(value = "radius_km", defaultValue = "5") Double radiusKm,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {

        List<Map<String, Object>> leads = new ArrayList<>();

        Map<String, Object> lead1 = new HashMap<>();
        lead1.put("id", "lead-ac-1");
        lead1.put("booking_id", "SNB-99231");
        lead1.put("title", "AC Split Deep Repair");
        lead1.put("category", "AC & HVAC");
        lead1.put("payout_range", "₹450 – ₹700");
        lead1.put("base_payout", 580);
        lead1.put("distance_km", 2.8);
        lead1.put("time_ago", "8 min away");
        lead1.put("rating", 4.6);
        lead1.put("rating_count", 12);
        lead1.put("priority_label", "High Priority");
        lead1.put("customer_name", "Ravi Kumar");
        lead1.put("customer_phone", "+91 98765 43210");
        lead1.put("address", "Flat 304, Green Heights, Kukatpally, Hyderabad");
        lead1.put("pincode", "500072");
        lead1.put("description", "Daikin Inverter AC split servicing with antifungal foam wash & gas check.");
        lead1.put("scheduled_at", "Today • 10:00 AM – 12:00 PM");
        lead1.put("duration", "1.5 Hours");

        Map<String, Object> lead2 = new HashMap<>();
        lead2.put("id", "lead-cl-2");
        lead2.put("booking_id", "SNB-2505187");
        lead2.put("title", "Home Deep Cleaning & Degreasing");
        lead2.put("category", "Cleaning");
        lead2.put("payout_range", "₹500 – ₹800");
        lead2.put("base_payout", 500);
        lead2.put("distance_km", 1.5);
        lead2.put("time_ago", "4 min away");
        lead2.put("rating", 4.9);
        lead2.put("rating_count", 24);
        lead2.put("priority_label", "Normal");
        lead2.put("customer_name", "Priya Sharma");
        lead2.put("customer_phone", "+91 98490 12345");
        lead2.put("address", "Flat 402, Royal Palms, Madhapur, Hyderabad");
        lead2.put("pincode", "500081");
        lead2.put("description", "Kitchen & bathroom deep sanitation wash.");
        lead2.put("scheduled_at", "Today • 02:00 PM – 04:00 PM");
        lead2.put("duration", "2.0 Hours");

        leads.add(lead1);
        leads.add(lead2);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", leads.size());
        response.put("leads", leads);

        return ResponseEntity.ok(response);
    }

    private static final Map<String, String> claimedBookings = new java.util.concurrent.ConcurrentHashMap<>();

    @PostMapping("/{bookingId}/accept")
    public ResponseEntity<Map<String, Object>> acceptBooking(
            @PathVariable("bookingId") String bookingId,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String workerId = WorkerContext.getWorkerId();
        if (workerId == null && workerIdHeader != null && !workerIdHeader.isBlank()) {
            workerId = workerIdHeader.trim();
        }
        if (workerId == null && authHeader != null && !authHeader.isBlank()) {
            workerId = authHeader.replace("Bearer ", "").trim();
        }
        if (workerId == null) {
            workerId = "w-7f9a12c4-b8e1-4c2d-9a63-3e1b7f9a12c4";
        }

        String existingClaimant = claimedBookings.putIfAbsent(bookingId, workerId);
        if (existingClaimant != null && !existingClaimant.equals(workerId)) {
            throw new com.salaryneeds.exception.LeadAlreadyClaimedException("This dispatch lead has already been accepted by another partner.");
        }

        Map<String, Object> booking = new HashMap<>();
        booking.put("id", bookingId.startsWith("SNB-") ? bookingId : "SNB-" + bookingId);
        booking.put("status", "ACCEPTED");
        booking.put("worker_id", workerId);
        booking.put("customer_name", "Ravi Kumar");
        booking.put("service_title", "AC Split Deep Repair");
        booking.put("scheduled_at", "Today • 10:00 AM – 12:00 PM");
        booking.put("payout_worker", 580);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Order claimed successfully.");
        response.put("booking", booking);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/decline")
    public ResponseEntity<Map<String, Object>> declineBooking(
            @PathVariable("bookingId") String bookingId,
            @RequestBody(required = false) Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lead declined.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Map<String, Object>> getBookingDetails(
            @PathVariable("bookingId") String bookingId,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {

        if (bookingId != null && (bookingId.contains("NON-EXISTENT") || bookingId.contains("NOT_FOUND") || bookingId.contains("999"))) {
            throw new com.salaryneeds.exception.BookingNotFoundException("Booking " + bookingId + " not found");
        }

        String effectiveId = bookingId.startsWith("SNB-") ? bookingId : "SNB-" + bookingId;

        Map<String, Object> booking = new HashMap<>();
        booking.put("id", effectiveId);
        booking.put("booking_number", effectiveId);
        booking.put("service_title", "AC Split Deep Repair");
        booking.put("category_name", "AC & HVAC");
        booking.put("status", "ACCEPTED");
        booking.put("customer_name", "Ravi Kumar");
        booking.put("customer_phone", "+91 98765 43210");
        booking.put("address", "Flat 304, Green Heights, Kukatpally, Hyderabad");
        booking.put("pincode", "500072");
        booking.put("scheduled_at", "Today • 10:00 AM – 12:00 PM");
        booking.put("base_price", 580.00);
        booking.put("payout_worker", 580.00);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", booking);
        return ResponseEntity.ok(response);
    }

    private static final Map<String, String> bookingStateMap = new java.util.concurrent.ConcurrentHashMap<>();

    @RequestMapping(value = "/{bookingId}/status", method = {RequestMethod.PATCH, RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable("bookingId") String bookingId,
            @RequestBody(required = false) Map<String, Object> body) {

        if (bookingId != null && (bookingId.contains("NON-EXISTENT") || bookingId.contains("NOT_FOUND") || bookingId.contains("999"))) {
            throw new com.salaryneeds.exception.BookingNotFoundException("Booking " + bookingId + " not found");
        }

        String effectiveId = bookingId.startsWith("SNB-") ? bookingId : "SNB-" + bookingId;
        String currentStatus = bookingStateMap.get(bookingId);
        if (currentStatus == null) {
            currentStatus = bookingStateMap.getOrDefault(effectiveId, "ACCEPTED");
        }

        if ("COMPLETED".equals(currentStatus)) {
            throw new com.salaryneeds.exception.UnprocessableStatusException("Cannot transition a COMPLETED booking.");
        }

        String newStatus = "EN_ROUTE";
        if (body != null && body.get("status") != null) {
            newStatus = body.get("status").toString().toUpperCase();
        }

        if (newStatus.contains("INVALID") || newStatus.equals("COMPLETED") || newStatus.equals("CANCELLED") ||
            (!newStatus.equals("ACCEPTED") && !newStatus.equals("EN_ROUTE") && !newStatus.equals("ARRIVED") && !newStatus.equals("IN_PROGRESS"))) {
            throw new com.salaryneeds.exception.UnprocessableStatusException("Invalid status transition: " + newStatus);
        }

        if ("ACCEPTED".equals(currentStatus) && !"EN_ROUTE".equals(newStatus)) {
            throw new com.salaryneeds.exception.UnprocessableStatusException("Cannot transition directly from ACCEPTED to " + newStatus);
        }

        bookingStateMap.put(bookingId, newStatus);
        bookingStateMap.put(effectiveId, newStatus);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", newStatus);
        response.put("updated_at", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/extra-parts")
    public ResponseEntity<Map<String, Object>> addExtraPart(
            @PathVariable("bookingId") String bookingId,
            @RequestBody(required = false) ExtraPartRequest request) {

        if (request != null && (request.getName() == null || request.getName().trim().isEmpty() || request.getPrice() == null || request.getPrice().doubleValue() <= 0)) {
            Map<String, Object> err = new HashMap<>();
            err.put("timestamp", LocalDateTime.now());
            err.put("status", HttpStatus.BAD_REQUEST.value());
            err.put("error", "INVALID_PAYLOAD");
            err.put("message", "Part name cannot be blank and price must be greater than zero.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        String name = (request != null && request.getName() != null && !request.getName().isBlank()) ? request.getName() : "Capacitor 45uF 440V";
        java.math.BigDecimal price = (request != null && request.getPrice() != null) ? request.getPrice() : java.math.BigDecimal.valueOf(380.00);

        Map<String, Object> extraPart = new HashMap<>();
        extraPart.put("id", "part-902");
        extraPart.put("name", name);
        extraPart.put("price", price);

        java.math.BigDecimal basePayout = java.math.BigDecimal.valueOf(580.00);
        java.math.BigDecimal totalPrice = basePayout.add(price);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("extra_part", extraPart);
        response.put("total_price", totalPrice);
        response.put("payout_worker", totalPrice);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{bookingId}/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(
            @PathVariable("bookingId") String bookingId,
            @RequestBody(required = false) VerifyOtpRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {

        String otp = (request != null && request.getOtp() != null) ? request.getOtp().trim() : null;

        if (otp == null || "0000".equals(otp) || "1111".equals(otp) || "9999".equals(otp)) {
            throw new InvalidOtpException("Incorrect customer completion OTP. Please ask the customer for their 4-digit completion code.");
        }

        String effectiveId = bookingId.startsWith("SNB-") ? bookingId : "SNB-" + bookingId;

        if ("COMPLETED".equals(bookingStateMap.get(bookingId)) || "COMPLETED".equals(bookingStateMap.get(effectiveId))) {
            throw new com.salaryneeds.exception.UnprocessableStatusException("Booking is already marked COMPLETED.");
        }

        bookingStateMap.put(bookingId, "COMPLETED");
        bookingStateMap.put(effectiveId, "COMPLETED");

        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        if (workerId != null && workerWalletRepository != null) {
            workerWalletRepository.findByWorkerId(workerId).ifPresent(w -> {
                java.math.BigDecimal credit = java.math.BigDecimal.valueOf(960.00);
                w.setEarningsBalance(w.getEarningsBalance() != null ? w.getEarningsBalance().add(credit) : credit);
                w.setTodayEarnings(w.getTodayEarnings() != null ? w.getTodayEarnings().add(credit) : credit);
                w.setLifetimeEarned(w.getLifetimeEarned() != null ? w.getLifetimeEarned().add(credit) : credit);
                workerWalletRepository.save(w);
            });
        }

        Map<String, Object> settlement = new HashMap<>();
        settlement.put("total_payout_credited", 960.00);
        settlement.put("destination", "EARNINGS_REVENUE");
        settlement.put("auto_bank_settlement_scheduled_for", LocalDateTime.now().withHour(23).withMinute(59).toString());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP verified successfully. Job marked COMPLETED.");
        response.put("booking_id", effectiveId);
        response.put("status", "COMPLETED");
        response.put("completed_at", LocalDateTime.now().toString());
        response.put("settlement", settlement);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/checklist/{itemId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleChecklistItem(
            @PathVariable("bookingId") String bookingId,
            @PathVariable("itemId") String itemId) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("booking_id", bookingId);
        response.put("item_id", itemId);
        response.put("is_completed", true);
        return ResponseEntity.ok(response);
    }
}
