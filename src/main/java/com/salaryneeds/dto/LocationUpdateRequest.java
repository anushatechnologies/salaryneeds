package com.salaryneeds.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class LocationUpdateRequest {
    private UUID booking_id;
    private Double lat;
    private Double lng;
}
