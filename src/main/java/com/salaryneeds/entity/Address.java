package com.salaryneeds.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ADDRESSES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "label")
    @Builder.Default
    private String label = "Home";

    @Column(name = "address_line", nullable = false)
    private String addressLine;

    @Column(name = "house")
    private String house;

    @Column(name = "street")
    private String street;

    @Column(name = "pincode", nullable = false)
    private String pincode;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String toFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (house != null && !house.isBlank()) {
            sb.append(house).append(", ");
        }
        if (street != null && !street.isBlank()) {
            sb.append(street).append(", ");
        }
        if (addressLine != null && !addressLine.isBlank()) {
            if (!sb.toString().contains(addressLine)) {
                sb.append(addressLine).append(", ");
            }
        }
        sb.append(city).append(" - ").append(pincode);
        return sb.toString();
    }
}
