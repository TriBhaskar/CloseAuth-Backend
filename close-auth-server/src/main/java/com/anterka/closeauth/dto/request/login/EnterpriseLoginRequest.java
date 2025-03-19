package com.anterka.closeauth.dto.request.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseLoginRequest {
    @NotNull(message = "Email cannot be null")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotNull(message = "password cannot be null")
    @NotBlank(message = "password cannot be empty")
    private String password;
}
