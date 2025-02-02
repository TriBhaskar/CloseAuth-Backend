package com.anterka.closeauth.dto.request.verifyotp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseVerifyOtpRequest {

    @NotBlank(message = "Email cannot be blank")
    private String email;
    @NotBlank(message = "Otp cannot be blank")
    private String otp;

}
