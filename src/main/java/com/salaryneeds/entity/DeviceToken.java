package com.salaryneeds.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceToken {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "worker_id", nullable = false, length = 64)
    private String workerId;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(length = 50)
    private String platform;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onSave() {
        updatedAt = LocalDateTime.now();
    }
}
