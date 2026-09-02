package com.salaryneeds.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateProfileRequest {
    private String skills;
    private Integer experience_years;
    private String pincode;
    private UUID category_id;
}
