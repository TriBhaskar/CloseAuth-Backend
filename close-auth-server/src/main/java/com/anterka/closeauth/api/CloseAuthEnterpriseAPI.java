package com.anterka.closeauth.api;

import com.anterka.closeauth.dto.request.auth.CloseAuthAuthenticationRequest;
import com.anterka.closeauth.dto.request.register.EnterpriseRegistrationRequest;
import com.anterka.closeauth.dto.response.CloseAuthAuthenticationResponse;
import com.anterka.closeauth.dto.response.EnterpriseRegistrationResponse;
import com.anterka.closeauth.service.CloseAuthAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(ApiPaths.API_PREFIX)
@RequiredArgsConstructor
public class CloseAuthEnterpriseAPI {

    private static final Logger log = LoggerFactory.getLogger(CloseAuthEnterpriseAPI.class);
    private final CloseAuthAuthenticationService authAuthenticationService;

    @PostMapping(ApiPaths.REGISTER_ENTERPRISE)
    public ResponseEntity<EnterpriseRegistrationResponse> register(@Valid @RequestBody EnterpriseRegistrationRequest request) {
        return ResponseEntity.ok(authAuthenticationService.registerEnterprise(request));
    }

    @PostMapping(ApiPaths.LOGIN)
    public ResponseEntity<CloseAuthAuthenticationResponse> login(@RequestBody CloseAuthAuthenticationRequest request) {
        log.info("Received authentication request : {}", request);
        return ResponseEntity.ok(authAuthenticationService.authenticate(request));
    }

}
