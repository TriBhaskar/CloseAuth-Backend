package com.anterka.closeauth.api;

import com.anterka.closeauth.dto.request.login.EnterpriseLoginRequest;
import com.anterka.closeauth.dto.request.register.EnterpriseRegistrationRequest;
import com.anterka.closeauth.dto.request.verifyotp.EnterpriseVerifyOtpRequest;
import com.anterka.closeauth.dto.request.verifyotp.EnterpriseResendOtpRequest;
import com.anterka.closeauth.dto.response.EnterpriseLoginResponse;
import com.anterka.closeauth.dto.response.CustomApiResponse;
import com.anterka.closeauth.dto.response.EnterpriseRegistrationResponse;
import com.anterka.closeauth.service.CloseAuthAuthenticationService;
import com.anterka.closeauth.service.RegistrationCacheService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.API_PREFIX)
@CrossOrigin(origins = "*", allowedHeaders = "*") // for dev-testing only
public class CloseAuthEnterpriseAPI {

    private static final Logger log = LoggerFactory.getLogger(CloseAuthEnterpriseAPI.class);
    private final CloseAuthAuthenticationService authAuthenticationService;
    private final RegistrationCacheService registrationCacheService;

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

    //for redis testing purpose
    @PostMapping("v1/testredis")
    public ResponseEntity<String> testRedis(@RequestBody EnterpriseRegistrationRequest request) {
        registrationCacheService.saveRegistration(request.getEnterpriseDetails().getEnterpriseEmail(), request);
        EnterpriseRegistrationRequest registration = registrationCacheService.getRegistration(request.getEnterpriseDetails().getEnterpriseEmail());
        return ResponseEntity.ok(registration.toString());
    }



}
