package com.anterka.closeauth.api;

import com.anterka.closeauth.api.constants.ResponseStatus;
import com.anterka.closeauth.dto.request.password.CloseAuthForgotPasswordRequest;
import com.anterka.closeauth.dto.request.password.CloseAuthResetPasswordRequest;
import com.anterka.closeauth.dto.response.CloseAuthForgotPasswordResponse;
import com.anterka.closeauth.dto.response.CloseAuthResetPasswordResponse;
import com.anterka.closeauth.dto.response.CloseAuthTokenValidationResponse;
import com.anterka.closeauth.exception.InvalidTokenException;
import com.anterka.closeauth.exception.PasswordMismatchedException;
import com.anterka.closeauth.exception.PasswordReusedException;
import com.anterka.closeauth.exception.WeakPasswordException;
import com.anterka.closeauth.service.CloseAuthPasswordResetService;
import com.anterka.closeauth.service.redis.CloseAuthRateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping(ApiPaths.API_PREFIX)
@CrossOrigin(origins = "*", allowedHeaders = "*")
@AllArgsConstructor
public class CloseAuthPasswordResetController {

    private final CloseAuthPasswordResetService passwordResetService;

    private final CloseAuthRateLimiterService rateLimiter;

    @PostMapping(ApiPaths.FORGOT_PASSWORD)
    public ResponseEntity<CloseAuthForgotPasswordResponse> forgotPassword(@RequestBody CloseAuthForgotPasswordRequest request,
                                                                          HttpServletRequest servletRequest) {
        // Rate limiting for forgot password requests
        String clientIp = getClientIp(servletRequest);
        if (rateLimiter.isLimited("forgot_password", clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new CloseAuthForgotPasswordResponse("Too many requests. Please try again later.", ResponseStatus.FAILED, LocalDateTime.now()));
        }
        try {
            passwordResetService.processForgotPassword(request);
            return ResponseEntity.ok().body(new CloseAuthForgotPasswordResponse("If your email is registered, you will receive a password reset link shortly", ResponseStatus.SUCCESS, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CloseAuthForgotPasswordResponse("If your email is registered, you will receive a password reset link shortly",ResponseStatus.FAILED,LocalDateTime.now()));
        }
    }

    @GetMapping(ApiPaths.VALIDATE_TOKEN)
    public ResponseEntity<CloseAuthTokenValidationResponse> validateToken(@RequestParam String token,
                                           HttpServletRequest servletRequest) {
        // Rate limiting for token validation
        String clientIp = getClientIp(servletRequest);
        if (rateLimiter.isLimited("validate_token", clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new CloseAuthTokenValidationResponse(false, "Too many attempts. Please try again later."));
        }

        CloseAuthTokenValidationResponse result = passwordResetService.validateToken(token);
        return ResponseEntity.ok().body(new CloseAuthTokenValidationResponse(result.isValid(), result.getMessage()));
    }

    @PostMapping(ApiPaths.RESET_PASSWORD)
    public ResponseEntity<CloseAuthResetPasswordResponse> resetPassword(@RequestBody CloseAuthResetPasswordRequest request,
                                                                        HttpServletRequest servletRequest) {
        // Rate limiting for reset password attempts
        String clientIp = getClientIp(servletRequest);
        if (rateLimiter.isLimited("reset_password", clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new CloseAuthResetPasswordResponse(ResponseStatus.FAILED, "Too many attempts. Please try again later."));
        }
        try {
            passwordResetService.resetPassword(request);
            return ResponseEntity.ok().body(new CloseAuthResetPasswordResponse(ResponseStatus.SUCCESS, "Password reset successful"));
        } catch (InvalidTokenException | PasswordMismatchedException |
                 WeakPasswordException | PasswordReusedException e) {
            return ResponseEntity.badRequest().body(new CloseAuthResetPasswordResponse(ResponseStatus.FAILED, "Exception occurred while resetting password : " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CloseAuthResetPasswordResponse(ResponseStatus.FAILED, "An unexpected error occurred. Please try again."));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}