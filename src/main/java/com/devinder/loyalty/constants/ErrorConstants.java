package com.devinder.loyalty.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorConstants {
    public static final String SYSTEM_ERROR = "ERR_SYSTEM_ERROR";
    public static final String VALIDATION_FAILED = "ERR_VALIDATION_FAILED";
    public static final String RESOURCE_NOT_FOUND = "ERR_RESOURCE_NOT_FOUND";
    public static final String BAD_REQUEST = "ERR_BAD_REQUEST";
    public static final String CONFLICT = "ERR_CONFLICT";
    public static final String UNAUTHORIZED = "ERR_UNAUTHORIZED";
    public static final String FORBIDDEN = "ERR_FORBIDDEN";

    // Filter fallback messages
    public static final String REQ_BODY_READ_ERROR = "[error reading request body]";
    public static final String RES_BODY_READ_ERROR = "[error reading response body]";
}
