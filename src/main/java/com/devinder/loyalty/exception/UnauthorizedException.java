package com.devinder.loyalty.exception;

import com.devinder.loyalty.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ErrorConstants.UNAUTHORIZED);
    }
}
