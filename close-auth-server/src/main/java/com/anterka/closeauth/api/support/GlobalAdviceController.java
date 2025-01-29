package com.anterka.closeauth.api.support;

import com.anterka.closeauth.exception.CredentialValidationException;
import com.anterka.closeauth.exception.EmailAlreadyExistsException;
import com.anterka.closeauth.exception.EnterpriseRegistrationException;
import com.anterka.closeauth.exception.UserAuthenticationException;
import com.anterka.closeauth.exception.UserNotFoundException;
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
        private LocalDateTime timestamp = LocalDateTime.now();
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CredentialValidationException.class)
    public ResponseEntity<ErrorResponse> handleCredentialValidationException(CredentialValidationException ex){
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex){
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUserAuthenticationException(UserAuthenticationException ex){
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EnterpriseRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleEnterpriseRegException(EnterpriseRegistrationException ex){
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}