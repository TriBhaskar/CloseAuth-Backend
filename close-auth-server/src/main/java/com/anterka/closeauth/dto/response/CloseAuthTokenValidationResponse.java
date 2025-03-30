package com.anterka.closeauth.dto.response;

import lombok.Data;

/**
 * This class will be used to return token validation results
 * */
@Data
public class CloseAuthTokenValidationResponse {
    private final boolean valid;
    private final String message;
}
