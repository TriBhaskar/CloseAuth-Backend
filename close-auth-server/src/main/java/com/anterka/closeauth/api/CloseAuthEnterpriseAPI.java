package com.anterka.closeauth.api;

import com.anterka.closeauth.api.constants.ResponseStatus;
import com.anterka.closeauth.dto.request.login.EnterpriseLoginRequest;
import com.anterka.closeauth.dto.request.password.CloseAuthForgotPasswordRequest;
import com.anterka.closeauth.dto.request.password.CloseAuthResetPasswordRequest;
import com.anterka.closeauth.dto.request.register.EnterpriseRegistrationRequest;
import com.anterka.closeauth.dto.request.verifyotp.EnterpriseVerifyOtpRequest;
import com.anterka.closeauth.dto.request.verifyotp.EnterpriseResendOtpRequest;
import com.anterka.closeauth.dto.response.CloseAuthForgotPasswordResponse;
import com.anterka.closeauth.dto.response.CloseAuthResetPasswordResponse;
import com.anterka.closeauth.dto.response.CloseAuthTokenValidationResponse;
import com.anterka.closeauth.dto.response.EnterpriseLoginResponse;
import com.anterka.closeauth.dto.response.CustomApiResponse;
import com.anterka.closeauth.dto.response.EnterpriseRegistrationResponse;
import com.anterka.closeauth.exception.InvalidTokenException;
import com.anterka.closeauth.exception.PasswordMismatchedException;
import com.anterka.closeauth.exception.PasswordReusedException;
import com.anterka.closeauth.exception.WeakPasswordException;
import com.anterka.closeauth.service.CloseAuthAuthenticationService;
import com.anterka.closeauth.service.CloseAuthPasswordResetService;
import com.anterka.closeauth.service.RegistrationCacheService;
import com.anterka.closeauth.service.redis.CloseAuthRateLimiterService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.API_PREFIX)
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CloseAuthEnterpriseAPI {

    private static final Logger log = LoggerFactory.getLogger(CloseAuthEnterpriseAPI.class);
    private final CloseAuthRateLimiterService rateLimiter;
    private final RegistrationCacheService registrationCacheService;
    private final CloseAuthPasswordResetService passwordResetService;
    private final CloseAuthAuthenticationService authAuthenticationService;

    @PostMapping(ApiPaths.REGISTER_ENTERPRISE)
    public ResponseEntity<EnterpriseRegistrationResponse> register(@Valid @RequestBody EnterpriseRegistrationRequest request) throws MessagingException {
        return ResponseEntity.ok(authAuthenticationService.registerEnterprise(request));
    }

    @PostMapping(ApiPaths.LOGIN)
    public ResponseEntity<EnterpriseLoginResponse> login(@RequestBody EnterpriseLoginRequest request) {
        log.info("Received authentication request : {}", request);
        return ResponseEntity.ok(authAuthenticationService.authenticate(request));
    }

    @PostMapping(ApiPaths.VERIFY_OTP)
    public ResponseEntity<CustomApiResponse> verifyOTP(@RequestBody EnterpriseVerifyOtpRequest request) {
        log.info("Received OTP verification request : {}", request);
        return ResponseEntity.ok(authAuthenticationService.verifyEnterpriseEmail(request));
    }

    @PostMapping(ApiPaths.RESEND_OTP)
    public ResponseEntity<CustomApiResponse> resendOTP(@RequestBody EnterpriseResendOtpRequest request) {
        log.info("Received OTP resend request : {}", request);
        return ResponseEntity.ok(authAuthenticationService.resendEnterpriseOTP(request));
    }

    @PostMapping(ApiPaths.FORGOT_PASSWORD)
    public ResponseEntity<CloseAuthForgotPasswordResponse> forgotPassword(@RequestBody CloseAuthForgotPasswordRequest request,
                                                                          HttpServletRequest servletRequest) {
        // Rate limiting for forgot password requests
        log.info("Received request for the forgot password");
        String clientIp = getClientIp(servletRequest);
        if (rateLimiter.isLimited("forgot_password", clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new CloseAuthForgotPasswordResponse("Too many requests. Please try again later.", com.anterka.closeauth.api.constants.ResponseStatus.FAILED, LocalDateTime.now()));
        }
        try {
            passwordResetService.processForgotPassword(request);
            return ResponseEntity.ok().body(new CloseAuthForgotPasswordResponse("If your email is registered, you will receive a password reset link shortly", com.anterka.closeauth.api.constants.ResponseStatus.SUCCESS, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CloseAuthForgotPasswordResponse("If your email is registered, you will receive a password reset link shortly", com.anterka.closeauth.api.constants.ResponseStatus.FAILED,LocalDateTime.now()));
        }
    }

    @PostMapping(ApiPaths.VALIDATE_TOKEN)
    public ResponseEntity<CloseAuthTokenValidationResponse> validateToken(@RequestBody String token,
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
                    .body(new CloseAuthResetPasswordResponse(com.anterka.closeauth.api.constants.ResponseStatus.FAILED, "Too many attempts. Please try again later."));
        }
        try {
            passwordResetService.resetPassword(request);
            return ResponseEntity.ok().body(new CloseAuthResetPasswordResponse(com.anterka.closeauth.api.constants.ResponseStatus.SUCCESS, "Password reset successful"));
        } catch (InvalidTokenException | PasswordMismatchedException |
                 WeakPasswordException | PasswordReusedException e) {
            return ResponseEntity.badRequest().body(new CloseAuthResetPasswordResponse(com.anterka.closeauth.api.constants.ResponseStatus.FAILED, "Exception occurred while resetting password : " + e.getMessage()));
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

    //for redis testing purpose
    @PostMapping("v1/testredis")
    public ResponseEntity<String> testRedis(@RequestBody EnterpriseRegistrationRequest request) {
        registrationCacheService.saveRegistration(request.getEnterpriseDetails().getEnterpriseEmail(), request);
        EnterpriseRegistrationRequest registration = registrationCacheService.getRegistration(request.getEnterpriseDetails().getEnterpriseEmail());
        return ResponseEntity.ok(registration.toString());
    }



}
