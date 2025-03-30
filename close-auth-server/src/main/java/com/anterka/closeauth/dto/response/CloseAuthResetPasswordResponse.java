package com.anterka.closeauth.dto.response;

import com.anterka.closeauth.api.constants.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CloseAuthResetPasswordResponse {
    private ResponseStatus status;
    private String message;
}
