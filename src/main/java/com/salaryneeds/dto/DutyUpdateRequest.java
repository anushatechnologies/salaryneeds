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
    @JsonAlias({"dutyOnline", "duty_online", "dutyStatus", "duty_status", "onDuty", "on_duty"})
    private Object dutyOnline;

    public Boolean isOnline() {
        if (dutyOnline == null) return true;
        if (dutyOnline instanceof Boolean b) return b;
        String s = dutyOnline.toString().trim().toUpperCase();
        return s.equals("ON_DUTY") || s.equals("TRUE") || s.equals("ONLINE") || s.equals("1");
    }
}

