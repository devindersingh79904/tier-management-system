package com.devinder.loyalty.exception;

import com.devinder.loyalty.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, ErrorConstants.CONFLICT);
    }
}
