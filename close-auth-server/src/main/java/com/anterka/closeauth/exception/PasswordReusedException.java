package com.anterka.closeauth.exception;

public class PasswordReusedException extends RuntimeException{
    public PasswordReusedException(String message){
        super(message);
    }
}
