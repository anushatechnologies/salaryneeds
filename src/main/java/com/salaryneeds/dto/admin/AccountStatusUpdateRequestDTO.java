package com.salaryneeds.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountStatusUpdateRequestDTO {

    /** ACTIVE or BLOCKED */
    @NotBlank(message = "accountStatus is required (ACTIVE or BLOCKED)")
    private String accountStatus;
}
