package com.anterka.closeauth.config;

import com.anterka.closeauth.api.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final AuthenticationProvider authenticationProvider;

    private final String[] skipAuthorizationForRequests = {
            ApiPaths.API_CONTEXT_PATH+ApiPaths.API_PREFIX+ApiPaths.LOGIN,
            ApiPaths.API_CONTEXT_PATH+ApiPaths.API_PREFIX+ApiPaths.REGISTER_ENTERPRISE,
            ApiPaths.API_CONTEXT_PATH+ApiPaths.API_PREFIX+ApiPaths.VERIFY_OTP,
            ApiPaths.API_CONTEXT_PATH+ApiPaths.API_PREFIX+ApiPaths.FORGOT_PASSWORD,
            ApiPaths.API_CONTEXT_PATH+ApiPaths.API_PREFIX+ApiPaths.VALIDATE_TOKEN,
            ApiPaths.API_CONTEXT_PATH+ApiPaths.API_PREFIX+ApiPaths.RESET_PASSWORD,
            ApiPaths.API_CONTEXT_PATH+ApiPaths.API_PREFIX+ApiPaths.RESEND_OTP,
            ApiPaths.API_CONTEXT_PATH+"/api/v1/testredis"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(skipAuthorizationForRequests).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
