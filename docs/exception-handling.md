# Exception Handling Strategy

This document details how errors are categorized, caught, logged, and mapped to consistent API responses in the Loyalty Tier System.

---

## 1. Custom Exception Hierarchy (BaseException)

To prevent database exceptions or system framework leakages from exposing implementation details to the API consumer, we use a centralized domain exception hierarchy:

```
                      ┌──────────────────────┐
                      │   RuntimeException   │
                      └──────────┬───────────┘
                                 ▼
                      ┌──────────────────────┐
                      │    BaseException     │ (Properties: HttpStatus, ErrorCode)
                      └──────────┬───────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         ▼                       ▼                       ▼
┌───────────────────┐   ┌───────────────────┐   ┌───────────────────┐
│ ResourceNotFound  │   │ ConflictException │   │BadRequestException│
└───────────────────┘   └───────────────────┘   └───────────────────┘
```

### BaseException Properties
All business exceptions extend the abstract class `BaseException`. It encapsulates two key fields:
*   `HttpStatus`: The HTTP status mapped for this domain event (e.g. `HttpStatus.NOT_FOUND`).
*   `errorCode`: A unique machine-readable error string mapped to front-end localized message translation (e.g. `ERR_CONFLICT`).

---

## 2. Centralized Exception Controller Advice

Instead of scattering try-catch blocks across controllers, the framework relies on `GlobalExceptionHandler` (annotated with `@RestControllerAdvice`) to capture and transform exceptions globally:

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
        log.warn("Business exception occurred: [Code: {}] {}", ex.getErrorCode(), ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), ex.getMessage(), ex.getStatus().value());
        return new ResponseEntity<>(response, ex.getStatus());
    }
}
```

### Translation Benefits
*   **Security:** System-level exception traces are blocked at the controller boundary.
*   **Uniformity:** Every error output uses the standard envelope format (`ApiResponse.error(...)`).
*   **Cleanliness:** Services and controllers simply throw custom exceptions when rules are violated, avoiding complex return-state mapping.

---

## 3. Validation Exception Handling

When a payload fails request constraints (e.g. `@NotNull`, `@NotBlank`), Spring throws a `MethodArgumentNotValidException`. 
*   The `GlobalExceptionHandler` catches this and extracts all field validation issues.
*   It aggregates them into a readable array of errors, mapping the response code directly to `400 Bad Request`:

```java
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
```

---

## 4. Unhandled and Infrastructure Errors

When unexpected runtime exceptions occur (such as database disconnection, null pointer references, or external server timeouts), Spring will catch them under the generic `Exception` fallback:

```java
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
```

### Safety Features
*   **Stack Trace Concealment:** The full raw Java exception stack trace is logged securely to the server console (allowing developers to troubleshoot using the corresponding `correlationId`), but it is **never** returned to the client.
*   **Fail-Safe Envelope:** The client receives a generic `ERR_SYSTEM_ERROR` message, protecting internal implementation details.

---

## 5. Mapping Static Resource & Missing Route Failures

In Spring Boot 3, requesting a non-existent URL or static asset (e.g., `/favicon.ico`) raises a `NoResourceFoundException`. 
Rather than mapping this to a generic `500 Internal Server Error` (which clutters application logs with false-alarm stack traces), a dedicated handler maps it to `404 Not Found` with a simple `WARN` logging level:

```java
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
                ErrorConstants.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
```
This isolates framework-level request routing misses from true system errors.
