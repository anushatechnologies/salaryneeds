package com.salaryneeds.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerRegisterRequest {

    @com.fasterxml.jackson.annotation.JsonAlias({"fullName", "full_name"})
    @NotBlank(message = "Name is required")
    private String name;

    @com.fasterxml.jackson.annotation.JsonAlias({"phoneNumber", "phone_number", "mobile"})
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+91)?[6-9]\\d{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    private String phone;

    @Email(message = "Invalid email address")
    private String email;

    private String trade;
    private String categoryId;
    private String categoryName;
    private String subCategoryId;
    private String subCategoryName;
    private Integer experienceYears;
    private String pincode;
    private String city;
    private String address;
    private List<String> serviceAreas;
    private List<String> skills;

    @com.fasterxml.jackson.annotation.JsonAlias({"aadhar_number", "aadharNo", "aadhar_no", "aadhaarNumber", "aadhaar_number", "aadhaarNo", "aadhaar_no"})
    private String aadharNumber;

    @com.fasterxml.jackson.annotation.JsonAlias({"pan_number", "panNo", "pan_no", "panCardNumber", "pan_card_number"})
    private String panNumber;

    @com.fasterxml.jackson.annotation.JsonAlias({"aadhar", "aadhar_url", "aadharCard", "aadhaar", "aadhaar_url"})
    private String aadharUrl;

    @com.fasterxml.jackson.annotation.JsonAlias({"pan", "pan_url", "panCard"})
    private String panUrl;

    private org.springframework.web.multipart.MultipartFile aadharFile;
    private org.springframework.web.multipart.MultipartFile panFile;

    public String getEffectiveAadharNumber() {
        if (aadharNumber != null && !aadharNumber.isBlank()) {
            return aadharNumber.replaceAll("\\s+", "").trim();
        }
        return null;
    }

    public String getEffectivePanNumber() {
        if (panNumber != null && !panNumber.isBlank()) {
            return panNumber.replaceAll("\\s+", "").trim().toUpperCase();
        }
        return null;
    }
}
