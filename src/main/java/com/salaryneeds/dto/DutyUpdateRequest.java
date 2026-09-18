package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DutyUpdateRequest {
    @JsonProperty("duty_online")
    @JsonAlias({"dutyOnline", "duty_online"})
    private Boolean dutyOnline;
}
