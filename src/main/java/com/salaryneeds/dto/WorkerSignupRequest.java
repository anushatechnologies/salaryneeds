package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerSignupRequest {

    @JsonAlias({"fullName", "full_name"})
    @NotBlank(message = "Name is required")
    private String name;

    @JsonAlias({"phone number", "phone_number", "phoneNumber", "mobile", "phone"})
    @NotBlank(message = "Phone number is required")
    private String phone;

    private String email;

    @JsonAlias({"verificationCode", "code"})
    private String otp;

    @JsonAlias({"categoryId", "category", "category_id"})
    private UUID category_id;
    private String categoryId;
    private String categoryName;

    @JsonAlias({"subcategoryId", "subCategoryId", "subCategory", "sub_category", "sub_category_id", "subCategoryName", "sub_category_name"})
    private String sub_category_id;
    private String subCategoryId;
    private String subCategoryName;

    private String service;
    private String trade;
    private String skills;

    @JsonAlias({"experience", "experienceYears", "experience_years", "experience_year"})
    private Integer experience_years;
    private Integer experienceYears;

    @NotBlank(message = "Pincode is required")
    @JsonAlias({"pinCode", "postal_code", "zipCode", "postalCode"})
    private String pincode;

    private String city;
    private String address;

    @JsonAlias({"aadhar number", "aadhar_number", "aadharNo", "aadhar_no", "aadhaarNumber", "aadhaar_number", "aadhaarNo", "aadhaar_no", "aadharCardNumber"})
    private String aadharNumber;

    @JsonAlias({"pan number", "pan_number", "panNo", "pan_no", "panCardNumber", "pan_card_number"})
    private String panNumber;

    @JsonAlias({"aadhar", "aadhar_url", "aadharCard", "aadhaar", "aadhaar_url"})
    private String aadharUrl;

    @JsonAlias({"pan", "pan_url", "panCard"})
    private String panUrl;

    private MultipartFile aadharFile;
    private MultipartFile panFile;

    // Form-data / Bean property binding setters
    public void setCategory(String category) {
        this.categoryId = category;
    }

    public void setSubcategoryId(String subcategoryId) {
        this.subCategoryId = subcategoryId;
    }

    public void setSubCategoryId(String subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public void setSubCategory(String subCategory) {
        this.subCategoryId = subCategory;
    }

    public void setExperience(Integer experience) {
        this.experienceYears = experience;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phone = phoneNumber;
    }

    public void setPhone_number(String phoneNumber) {
        this.phone = phoneNumber;
    }

    public void setMobile(String mobile) {
        this.phone = mobile;
    }

    public void setAadhar_number(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public void setAadharNo(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadharNumber = aadhaarNumber;
    }

    public void setAadhaar_number(String aadhaarNumber) {
        this.aadharNumber = aadhaarNumber;
    }

    public void setAadhaarNo(String aadhaarNumber) {
        this.aadharNumber = aadhaarNumber;
    }

    public void setPan_number(String panNumber) {
        this.panNumber = panNumber;
    }

    public void setPanNo(String panNumber) {
        this.panNumber = panNumber;
    }

    public void setPinCode(String pinCode) {
        this.pincode = pinCode;
    }

    public void setPostal_code(String postalCode) {
        this.pincode = postalCode;
    }

    public void setZipCode(String zipCode) {
        this.pincode = zipCode;
    }

    public String getEffectiveAadharNumber() {
        if (aadharNumber != null && !aadharNumber.isBlank()) {
            return aadharNumber.replaceAll("[\\s-]+", "").trim();
        }
        return null;
    }

    public String getEffectivePanNumber() {
        if (panNumber != null && !panNumber.isBlank()) {
            return panNumber.replaceAll("[\\s-]+", "").trim().toUpperCase();
        }
        return null;
    }

    public String getEffectiveEmail() {
        return (email != null && !email.isBlank()) ? email.trim() : null;
    }

    public UUID getEffectiveCategoryId() {
        if (category_id != null) return category_id;
        if (categoryId != null && !categoryId.isBlank()) {
            return com.salaryneeds.util.UuidUtil.parseUuid(categoryId);
        }
        return null;
    }

    public Integer getEffectiveExperience() {
        if (experience_years != null) return experience_years;
        if (experienceYears != null) return experienceYears;
        return 0;
    }

    public String getEffectiveService() {
        if (service != null && !service.isBlank()) return service.trim();
        if (subCategoryName != null && !subCategoryName.isBlank()) return subCategoryName.trim();
        if (trade != null && !trade.isBlank()) return trade.trim();
        return "Technician";
    }
}
