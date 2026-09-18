package com.salaryneeds.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_checklists")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingChecklist {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "booking_id", nullable = false, length = 64)
    @com.fasterxml.jackson.annotation.JsonProperty("booking_id")
    @com.fasterxml.jackson.annotation.JsonAlias({"bookingId", "booking_id"})
    private String bookingId;

    @Column(name = "task_label", nullable = false, length = 255)
    @com.fasterxml.jackson.annotation.JsonProperty("task_label")
    @com.fasterxml.jackson.annotation.JsonAlias({"taskLabel", "task_label"})
    private String taskLabel;

    @Column(name = "is_completed", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("is_completed")
    @com.fasterxml.jackson.annotation.JsonAlias({"isCompleted", "is_completed"})
    @Builder.Default
    private Boolean isCompleted = false;
}
