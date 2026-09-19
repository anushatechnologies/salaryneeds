package com.salaryneeds.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_extra_parts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingExtraPart {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "booking_id", nullable = false, length = 64)
    private String bookingId;

    @Column(name = "part_name", nullable = false, length = 150)
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @com.fasterxml.jackson.annotation.JsonAlias({"partName", "part_name", "name"})
    private String partName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
