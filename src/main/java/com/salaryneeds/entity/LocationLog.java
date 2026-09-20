package com.salaryneeds.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "location_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationLog {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "worker_id", nullable = false, length = 64)
    private String workerId;

    @Column(name = "booking_id", length = 64)
    private String bookingId;

    private Double lat;
    private Double lng;
    private Double heading;
    private Double speed;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
