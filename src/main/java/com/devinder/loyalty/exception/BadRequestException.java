package com.devinder.loyalty.exception;

import com.devinder.loyalty.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorConstants.BAD_REQUEST);
    }
}
