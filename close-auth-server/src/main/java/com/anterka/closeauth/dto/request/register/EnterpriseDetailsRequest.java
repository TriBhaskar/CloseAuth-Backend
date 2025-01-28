package com.anterka.closeauth.dto.request.register;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseDetailsRequest {
    @NotBlank(message = "Enterprise name is required")
    private String enterpriseName;

    @Email(message = "Invalid email format")
    private String enterpriseEmail; // Optional

    @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Invalid contact number")
    private String enterpriseContactNumber;

    @NotBlank(message = "Country is required")
    private String enterpriseCountry;

    @NotBlank(message = "State is required")
    private String enterpriseState;

    @NotBlank(message = "City is required")
    private String enterpriseCity;

    @NotBlank(message = "Pin code is required")
    @Pattern(regexp = "^[0-9]{5,6}$", message = "Invalid pin code")
    private String enterprisePinCode;

    @NotBlank(message = "Address is required")
    private String enterpriseAddress;
}