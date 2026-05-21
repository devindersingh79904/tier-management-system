# Authentication Flow

This document details the step-by-step lifecycle of user registration and session initiation in the Loyalty Tier System.

---

## 1. User Registration (Signup) Flow

The signup flow allows a new customer to register on the platform. By default, self-registered users are assigned the `USER` role.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client App
    participant Controller as AuthController
    participant Service as AuthServiceImpl
    participant DB as PostgreSQL Database
    participant JWT as JwtUtil

    Client->>Controller: POST /api/v1/auth/signup (Name, Mobile, Password)
    Note over Controller: Validates inputs using JSR-380 annotations
    Controller->>Service: signup(SignupRequest)
    Service->>DB: Check if mobile number exists
    alt Mobile Number Already Exists
        DB-->>Service: User exists
        Service-->>Client: Throw ConflictException (409 Conflict)
    else Mobile Number Unique
        DB-->>Service: User does not exist
        Note over Service: Hash password with BCrypt
        Service->>DB: Save new User entity (default ROLE_USER)
        DB-->>Service: User saved (UUID generated)
        Service->>JWT: generateAccessToken(userId, mobile, role)
        JWT-->>Service: Access Token
        Service->>JWT: generateRefreshToken(mobile)
        JWT-->>Service: Refresh Token
        Service-->>Controller: Return AuthResponse
        Controller-->>Client: Return 201 Created (ApiResponse<AuthResponse>)
    end
```

### Signup Endpoint
*   **Method/Path:** `POST /api/v1/auth/signup`
*   **Public Access:** Yes
*   **Response Status:** `201 Created`

---

## 2. Session Initiation (Login) Flow

Users authenticate by providing their registered mobile number and password.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client App
    participant Controller as AuthController
    participant Service as AuthServiceImpl
    participant DB as PostgreSQL Database
    participant JWT as JwtUtil

    Client->>Controller: POST /api/v1/auth/login (Mobile, Password)
    Note over Controller: Validates inputs
    Controller->>Service: login(LoginRequest)
    Service->>DB: findByMobileNumber(mobileNumber)
    alt User Not Found
        DB-->>Service: Empty Optional
        Service-->>Client: Throw UnauthorizedException (401 Unauthorized)
    else User Found
        DB-->>Service: Return User Entity
        Note over Service: Verify password using passwordEncoder.matches()
        alt Password Mismatch
            Service-->>Client: Throw UnauthorizedException (401 Unauthorized)
        else Password Valid
            Service->>JWT: generateAccessToken(userId, mobile, role)
            JWT-->>Service: Access Token
            Service->>JWT: generateRefreshToken(mobile)
            JWT-->>Service: Refresh Token
            Service-->>Controller: Return AuthResponse
            Controller-->>Client: Return 200 OK (ApiResponse<AuthResponse>)
        end
    end
```

### Login Endpoint
*   **Method/Path:** `POST /api/v1/auth/login`
*   **Public Access:** Yes
*   **Response Status:** `200 OK`
