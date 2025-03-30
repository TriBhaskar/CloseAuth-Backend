package com.anterka.closeauth.exception;

public class PasswordMismatchedException extends RuntimeException{
    public PasswordMismatchedException(String message){
        super(message);
    }
}
