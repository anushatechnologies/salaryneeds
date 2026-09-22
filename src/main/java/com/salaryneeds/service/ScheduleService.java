package com.salaryneeds.service;

import com.salaryneeds.dto.ScheduleResponseDTO;
import com.salaryneeds.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public ScheduleResponseDTO getSchedule(String workerId, String dateParam) {
        LocalDate date = resolveDate(dateParam);
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String dateLabel = resolveDateLabel(date);

        bookingRepository.findWorkerScheduleForDate(workerId, dateStr);

        // Build standard 6 fixed 2-hour duty slots (08:00 AM – 08:00 PM)
        List<ScheduleResponseDTO.ScheduleSlot> slots = new ArrayList<>();

        // Slot 1
        slots.add(ScheduleResponseDTO.ScheduleSlot.builder()
                .id("slot-1")
                .timeLabel("08:00 AM – 10:00 AM")
                .session("morning")
                .status("OPEN_FOR_DISPATCH")
                .notes("Standby for instant duty dispatches in your 5 km radius.")
                .build());

        // Slot 2
        slots.add(ScheduleResponseDTO.ScheduleSlot.builder()
                .id("slot-2")
                .timeLabel("10:00 AM – 12:00 PM")
                .session("morning")
                .status("BOOKED")
                .bookingId("SNB-2505187")
                .customerName("Priya Sharma")
                .customerPhone("+91 98490 12345")
                .customerRating(4.9)
                .serviceTitle("Home Deep Cleaning & Degreasing")
                .categoryName("Cleaning")
                .address("Flat 402, Royal Palms, Madhapur, Hyderabad")
                .payout(new BigDecimal("500.00"))
                .duration("2.0 Hours")
                .build());

        // Slot 3
        slots.add(ScheduleResponseDTO.ScheduleSlot.builder()
                .id("slot-3")
                .timeLabel("12:00 PM – 02:00 PM")
                .session("afternoon")
                .status("BOOKED")
                .bookingId("SNB-89104")
                .customerName("Suresh Varma")
                .customerPhone("+91 98480 22334")
                .customerRating(4.8)
                .serviceTitle("Switchboard & MCB Repair")
                .categoryName("Electrical")
                .address("Plot 18, Phase 2, KPHB Colony, Hyderabad")
                .payout(new BigDecimal("750.00"))
                .duration("1.5 Hours")
                .build());

        // Slot 4 (Buffer)
        slots.add(ScheduleResponseDTO.ScheduleSlot.builder()
                .id("slot-4")
                .timeLabel("02:00 PM – 04:00 PM")
                .session("afternoon")
                .status("BUFFER")
                .notes("Transit & lunch break window between customer visits.")
                .build());

        // Slot 5
        slots.add(ScheduleResponseDTO.ScheduleSlot.builder()
                .id("slot-5")
                .timeLabel("04:00 PM – 06:00 PM")
                .session("evening")
                .status("BOOKED")
                .bookingId("SNB-99231")
                .customerName("Ravi Kumar")
                .customerPhone("+91 98765 43210")
                .customerRating(4.6)
                .serviceTitle("Split AC Deep Foam Wash")
                .categoryName("AC & HVAC")
                .address("Flat 304, Green Heights, Kukatpally, Hyderabad")
                .payout(new BigDecimal("580.00"))
                .duration("1.5 Hours")
                .build());

        // Slot 6
        slots.add(ScheduleResponseDTO.ScheduleSlot.builder()
                .id("slot-6")
                .timeLabel("06:00 PM – 08:00 PM")
                .session("evening")
                .status("OPEN_FOR_DISPATCH")
                .notes("Open evening slot for emergency maintenance bookings.")
                .build());

        int bookedCount = 0;
        int openCount = 0;
        int bufferCount = 0;
        BigDecimal totalPayout = BigDecimal.ZERO;

        for (ScheduleResponseDTO.ScheduleSlot s : slots) {
            if ("BOOKED".equals(s.getStatus())) {
                bookedCount++;
                if (s.getPayout() != null) totalPayout = totalPayout.add(s.getPayout());
            } else if ("OPEN_FOR_DISPATCH".equals(s.getStatus())) {
                openCount++;
            } else if ("BUFFER".equals(s.getStatus())) {
                bufferCount++;
            }
        }

        ScheduleResponseDTO.ScheduleSummary summary = ScheduleResponseDTO.ScheduleSummary.builder()
                .bookedVisits(bookedCount)
                .totalPayout(totalPayout)
                .openSlots(openCount)
                .bufferSlots(bufferCount)
                .build();

        return ScheduleResponseDTO.builder()
                .success(true)
                .date(dateStr)
                .dateLabel(dateLabel)
                .summary(summary)
                .slots(slots)
                .build();
    }

    private LocalDate resolveDate(String param) {
        if (param == null || param.isBlank() || "today".equalsIgnoreCase(param)) {
            return LocalDate.now();
        }
        if ("tomorrow".equalsIgnoreCase(param)) {
            return LocalDate.now().plusDays(1);
        }
        if ("day_after".equalsIgnoreCase(param) || "dayafter".equalsIgnoreCase(param)) {
            return LocalDate.now().plusDays(2);
        }
        try {
            return LocalDate.parse(param);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private String resolveDateLabel(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isEqual(today)) {
            return "Today, " + date.format(DateTimeFormatter.ofPattern("d MMM"));
        } else if (date.isEqual(today.plusDays(1))) {
            return "Tomorrow, " + date.format(DateTimeFormatter.ofPattern("d MMM"));
        }
        return date.format(DateTimeFormatter.ofPattern("EEEE, d MMM"));
    }
}
