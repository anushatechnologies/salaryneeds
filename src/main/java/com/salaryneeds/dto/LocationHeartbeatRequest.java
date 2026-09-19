package com.salaryneeds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationHeartbeatRequest {
    private Double lat;
    private Double lng;
    private Double heading;
    private Double speed;
    private Integer batteryLevel;

    public Double getSpeedKmh() {
        return speed;
    }

    public Integer getBatteryPct() {
        return batteryLevel;
    }

    public Boolean getIsMoving() {
        return speed != null && speed > 1.0;
    }
}
