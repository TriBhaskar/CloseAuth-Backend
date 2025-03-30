package com.anterka.closeauth.dto.request.password;

import lombok.Data;

@Data
public class CloseAuthResetPasswordRequest {
    private String token;
    private String newPassword;
    private String confirmPassword;
}
