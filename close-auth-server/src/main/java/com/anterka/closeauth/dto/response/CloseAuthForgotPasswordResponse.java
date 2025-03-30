package com.anterka.closeauth.dto.response;

import com.anterka.closeauth.api.constants.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CloseAuthForgotPasswordResponse {
    private String message;
    private ResponseStatus status;
    private LocalDateTime timeStamp;
}
