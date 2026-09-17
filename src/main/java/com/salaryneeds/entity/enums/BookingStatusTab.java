package com.salaryneeds.entity.enums;

import java.util.Arrays;
import java.util.List;

public enum BookingStatusTab {
    UPCOMING(Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.ASSIGNED)),
    ACTIVE(Arrays.asList(BookingStatus.WORKER_ON_THE_WAY, BookingStatus.ARRIVED, BookingStatus.IN_PROGRESS)),
    COMPLETED(Arrays.asList(BookingStatus.COMPLETED)),
    CANCELLED(Arrays.asList(BookingStatus.CANCELLED));

    private final List<BookingStatus> statuses;

    BookingStatusTab(List<BookingStatus> statuses) {
        this.statuses = statuses;
    }

    public List<BookingStatus> getStatuses() {
        return statuses;
    }
}
