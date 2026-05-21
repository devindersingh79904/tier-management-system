# API Guidelines - Authentication & Authorization

This document specifies the REST API endpoints, schemas, headers, status codes, and error payloads for the Authentication & Authorization module.

---

## 1. Request Headers

| Header | Required | Value / Format | Description |
| :--- | :--- | :--- | :--- |
| `Content-Type` | Yes | `application/json` | Request payload format. |
| `Authorization` | Yes (for protected endpoints) | `Bearer <token>` | Access token received from the signup/login API. |

---

## 2. API Endpoints

### 2.1 User Signup

Creates a new user profile with a default `USER` role and initiates a session.

*   **Endpoint:** `POST /api/v1/auth/signup`
*   **Authentication Required:** No
*   **Request Body:**
```json
{
  "name": "Jane Doe",
  "mobileNumber": "9876543210",
  "password": "securePassword123"
}
```

*   **Response (201 Created):**
```json
{
  "message": "User registered successfully",
  "status": 201,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5ODc2NTQzMjEwIi...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5ODc2NTQzMjEwIi...",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 18000,
    "refreshTokenExpiresIn": 2592000
  },
  "errors": [],
  "correlationId": "f7d3a822-45e3-40a1-8d23-...",
  "timestamp": "2026-05-21T21:32:07.000Z"
}
```

*   **Error Response (409 Conflict - Duplicate Mobile):**
```json
{
  "message": "Mobile number is already registered",
  "status": 409,
  "data": null,
  "errors": [
    "ERR_CONFLICT"
  ],
  "correlationId": "f7d3a822-45e3-40a1-8d23-...",
  "timestamp": "2026-05-21T21:32:08.000Z"
}
```

---

### 2.2 User Login

Validates mobile credentials and returns a new session token payload.

*   **Endpoint:** `POST /api/v1/auth/login`
*   **Authentication Required:** No
*   **Request Body:**
```json
{
  "mobileNumber": "9876543210",
  "password": "securePassword123"
}
```

*   **Response (200 OK):**
```json
{
  "message": "User logged in successfully",
  "status": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5ODc2NTQzMjEwIi...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5ODc2NTQzMjEwIi...",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 18000,
    "refreshTokenExpiresIn": 2592000
  },
  "errors": [],
  "correlationId": "f7d3a822-45e3-40a1-8d23-...",
  "timestamp": "2026-05-21T21:32:10.000Z"
}
```

*   **Error Response (401 Unauthorized - Bad Credentials):**
```json
{
  "message": "Invalid mobile number or password",
  "status": 401,
  "data": null,
  "errors": [
    "ERR_UNAUTHORIZED"
  ],
  "correlationId": "f7d3a822-45e3-40a1-8d23-...",
  "timestamp": "2026-05-21T21:32:11.000Z"
}
```

---

## 3. Validation Rules

Validation rules are enforced on request payloads prior to controller routing. If validation fails, a `400 Bad Request` status is returned with a descriptive error list:

*   **Name:** Required (`@NotBlank`), maximum 100 characters (`@Size`).
*   **Mobile Number:** Required (`@NotBlank`), must contain between 10 and 15 digits (`@Pattern`).
*   **Password:** Required (`@NotBlank`), between 8 and 100 characters (`@Size`).

### Example Validation Error Response (400 Bad Request):
```json
{
  "message": "Input validation failed. Please check details.",
  "status": 400,
  "data": null,
  "errors": [
    "password: Password must be between 8 and 100 characters",
    "mobileNumber: Mobile number must be between 10 and 15 digits"
  ],
  "correlationId": "f7d3a822-45e3-40a1-8d23-...",
  "timestamp": "2026-05-21T21:32:12.000Z"
}
```
