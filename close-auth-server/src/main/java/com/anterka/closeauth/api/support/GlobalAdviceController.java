package com.anterka.closeauth.api.support;

import com.anterka.closeauth.exception.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalAdviceController extends RuntimeException {
    @Data
    @Builder
    @RequiredArgsConstructor
    public static class ErrorResponse {
        private final String message;
        private final HttpStatus status;
        private final LocalDateTime timestamp = LocalDateTime.now();
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CredentialValidationException.class)
    public ResponseEntity<ErrorResponse> handleCredentialValidationException(CredentialValidationException ex){
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(DataAlreadyExistsException ex){
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUserAuthenticationException(UserAuthenticationException ex){
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EnterpriseRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleEnterpriseRegException(EnterpriseRegistrationException ex){
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EmailVerificationException.class)
    public ResponseEntity<ErrorResponse> handleEmailVerificationException(EmailVerificationException ex){
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.EXPECTATION_FAILED), HttpStatus.EXPECTATION_FAILED);
    }

}