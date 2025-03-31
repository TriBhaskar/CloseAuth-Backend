package com.anterka.closeauth.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class CloseAuthAppConfig {

    @Value("${app.password-reset.token.expiry}")
    private String tokenExpiryMinutes;
}
