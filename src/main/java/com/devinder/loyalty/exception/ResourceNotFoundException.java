package com.devinder.loyalty.exception;

import com.devinder.loyalty.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ErrorConstants.RESOURCE_NOT_FOUND);
    }
}
