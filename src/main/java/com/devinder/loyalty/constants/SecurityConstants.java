package com.devinder.loyalty.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityConstants {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_MOBILE = "mobileNumber";
    public static final String CLAIM_ROLE = "role";
    public static final String TOKEN_TYPE = "Bearer";

    // Exception messages
    public static final String INVALID_CREDENTIALS = "Invalid mobile number or password";
    public static final String DUPLICATE_MOBILE = "Mobile number is already registered";
    public static final String EXPIRED_TOKEN = "Token has expired";
    public static final String INVALID_TOKEN = "Invalid or malformed token";
    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
}
