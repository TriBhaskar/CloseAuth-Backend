package com.anterka.closeauth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnterpriseLoginResponse {
    private String status;
    private String message;
    private EnterpriseLoginData data;
}
