package com.salaryneeds.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankUpdateRequest {

    @NotBlank(message = "Bank name is required")
    @JsonProperty("bank_name")
    @JsonAlias({"bankName", "bank_name"})
    private String bankName;

    @NotBlank(message = "Account number is required")
    @JsonProperty("account_number")
    @JsonAlias({"accountNumber", "account_number"})
    private String accountNumber;

    @NotBlank(message = "IFSC code is required")
    private String ifsc;

    @NotBlank(message = "Account holder name is required")
    @JsonProperty("account_holder")
    @JsonAlias({"accountHolder", "account_holder"})
    private String accountHolder;
}
