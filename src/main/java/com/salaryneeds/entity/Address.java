package com.salaryneeds.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String label;

    @Column(name = "address_line", nullable = false)
    private String addressLine;

    @Column(name = "pincode", nullable = false)
    private String pincode;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

}
