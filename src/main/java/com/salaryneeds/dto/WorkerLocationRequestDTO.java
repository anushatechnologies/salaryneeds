package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkerLocationRequestDTO {

    @NotNull(message = "Latitude is required")
    @JsonAlias({"latitude", "lat"})
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @JsonAlias({"longitude", "lng"})
    private Double longitude;

    private Double accuracy;
}
