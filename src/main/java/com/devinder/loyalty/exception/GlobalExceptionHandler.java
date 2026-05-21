package com.devinder.loyalty.exception;

import com.devinder.loyalty.constants.ErrorConstants;
import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
        log.warn("Business exception occurred: [Code: {}] {}", ex.getErrorCode(), ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), ex.getMessage(), ex.getStatus().value());
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied error: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(
                ErrorConstants.FORBIDDEN,
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failure: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(
                ErrorConstants.UNAUTHORIZED,
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        log.warn("Validation failed: {}", errors);

        ApiResponse<Void> response = ApiResponse.error(
                errors,
                MessageConstants.VALIDATION_ERROR,
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        log.error("Concurrent update conflict (optimistic lock failure): {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                MessageConstants.CONFLICT_LOCKING,
                ErrorConstants.CONFLICT,
                HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ErrorConstants.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected server error: ", ex);

        ApiResponse<Void> response = ApiResponse.error(
                ErrorConstants.SYSTEM_ERROR,
                MessageConstants.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
