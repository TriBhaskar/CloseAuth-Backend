package com.anterka.closeauth.dto.response;

import com.anterka.closeauth.api.constants.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseRegistrationResponse {
    private ResponseStatus status;
    private String message;
    private String username;
    private long otpValiditySeconds;
    private LocalDateTime timestamp;
}