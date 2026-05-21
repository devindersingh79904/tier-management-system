# Security Guide

This document outlines the security architecture, authentication flow, and role-based access rules implemented in the Loyalty Tier System.

---

## 1. Authentication mechanism

All API requests (except public endpoints) are authenticated using **JSON Web Tokens (JWT)**:

1.  **State-free:** No HTTP session state is stored on the server.
2.  **Bearer authentication:** Client must include the access token in the `Authorization` header formatted as:
    `Authorization: Bearer <JWT_TOKEN>`
3.  **Token Processing:** The `JwtAuthenticationFilter` intercepts the request, validates the token signature/expiry, extracts the user principal, and sets it in the Spring Security context.

---

## 2. Secure Security Context Extraction (/me endpoints)

For self-service user endpoints (mapped under `/api/v1/me/**`), the controller and service must **never** trust user IDs passed as query parameters or in request payloads from the client.

### Guideline
*   Always extract the user identity dynamically from the authenticated Spring Security context principal.
*   The principal's `username` stores the unique `mobileNumber` of the user.

### Code Pattern
```java
// Extracting mobile number from security context
String mobileNumber = SecurityContextHolder.getContext().getAuthentication().getName();

// Lookup entity based on extracted mobile number
User user = userRepository.findByMobileNumber(mobileNumber)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with mobile number: " + mobileNumber));
```

---

## 3. Method-Level Security (@PreAuthorize)

Access control rules are enforced at the method or controller level using `@PreAuthorize`:

### Admin Privileged Controllers
Controllers under `com.devinder.loyalty.controller.admin` require either the `ADMIN` or `SUPER_ADMIN` role:
```java
@RestController
@RequestMapping("/api/v1/admin/...")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminController { ... }
```

### User Self-Service Controllers
Controllers under `com.devinder.loyalty.controller.user` require the request to be authenticated:
```java
@RestController
@RequestMapping("/api/v1/me/...")
@PreAuthorize("isAuthenticated()")
public class UserController { ... }
```

---

## 4. Security Configuration

Route permission configurations are specified in `SecurityConfig.java`:
*   Endpoints permitted without authentication: `/api/v1/auth/signup`, `/api/v1/auth/login`, and Swagger UI documentation paths.
*   All other paths default to requiring authenticated context checks.
