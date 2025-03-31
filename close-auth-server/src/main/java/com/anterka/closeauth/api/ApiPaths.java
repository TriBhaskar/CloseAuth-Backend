package com.anterka.closeauth.api;

/**
 * This holds the static API paths with the versioning as [v1]
 * Updated functionality in the APIs in any of the future release can be
 * versioned as [v2] and so on ...
 * */
public class ApiPaths {
    public static final String API_CONTEXT_PATH = "/cauth";
    public static final String API_PREFIX = "/api/";
    public static final String LOGIN = "v1/login";
    public static final String REGISTER_ENTERPRISE = "v1/register";
    public static final String VERIFY_OTP = "v1/verify-otp";
    public static final String RESEND_OTP = "v1/resend-otp";
    public static final String FORGOT_PASSWORD = "v1/forgot-password";
    public static final String VALIDATE_TOKEN = "v1/validate-reset-token";
    public static final String RESET_PASSWORD = "v1/reset-password";
    public static final String UPDATE_ENTERPRISE = "v1/update/enterprise";

    private ApiPaths(){}
}
