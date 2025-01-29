package com.anterka.closeauth.dto.request.register;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseRegistrationRequest {
    @NotBlank(message = "First name is required")
    private String userFirstName;

    @NotBlank(message = "Last name is required")
    private String userLastName;

    @NotBlank(message = "Username is required")
    private String userName;

    @NotBlank(message = "Password is required")
    private String userPassword;

    @Valid
    @NotNull(message = "Enterprise details are required")
    private EnterpriseDetailsRequest enterpriseDetails;
}
