package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationPingRequest {

    @NotNull(message = "Latitude is required")
    @JsonAlias({"lat", "latitude"})
    private Double lat;

    @NotNull(message = "Longitude is required")
    @JsonAlias({"lng", "longitude"})
    private Double lng;

    private Double speed;
    private Double heading;

    @JsonProperty("active_booking_id")
    @JsonAlias({"activeBookingId", "active_booking_id"})
    private String activeBookingId;
}
