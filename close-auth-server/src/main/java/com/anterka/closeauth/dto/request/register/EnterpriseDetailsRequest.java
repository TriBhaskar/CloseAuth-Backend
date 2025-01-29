package com.anterka.closeauth.dto.request.register;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseDetailsRequest {
    @NotBlank(message = "Enterprise name is required")
    private String enterpriseName;

    @NotBlank(message = "Enterprise email is required")
    @Email(message = "Invalid email format")
    private String enterpriseEmail;

    @NotBlank(message = "Phone number is required")
    private String enterpriseContactNumber;

    @NotBlank(message = "Country is required")
    private String enterpriseCountry;

    @NotBlank(message = "State is required")
    private String enterpriseState;

    @NotBlank(message = "City is required")
    private String enterpriseCity;

    @NotBlank(message = "Pin code is required")
    private String enterprisePinCode;

    @NotBlank(message = "Address is required")
    private String enterpriseAddress;
}