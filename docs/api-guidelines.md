# API Guidelines

This document specifies the REST API endpoints, schemas, headers, status codes, and error payloads for all modules of the Loyalty Tier System.

---

## 1. Request Headers

| Header | Required | Value / Format | Description |
| :--- | :--- | :--- | :--- |
| `Content-Type` | Yes | `application/json` | Request payload format. |
| `Authorization` | Yes (for protected endpoints) | `Bearer <token>` | Access token received from the signup/login API. |
| `X-Correlation-Id` | No | `String (UUID)` | Optional correlation ID for distributed tracing. |

---

## 2. API Endpoints

### 2.1 Public Authentication APIs

#### 2.1.1 User Signup
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
  "correlationId": "f7d3a822-45e3-40a1-8d23-f72834b9281a",
  "timestamp": "2026-05-21T21:32:07.000Z"
}
```

#### 2.1.2 User Login
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
  "correlationId": "f7d3a822-45e3-40a1-8d23-f72834b9281a",
  "timestamp": "2026-05-21T21:32:10.000Z"
}
```

---

### 2.2 User Self-Service APIs

These endpoints use the caller's JWT security context to identify the logged-in user dynamically.

#### 2.2.1 Get Current User Profile
Retrieves profile details of the authenticated user.
*   **Endpoint:** `GET /api/v1/me/profile`
*   **Authentication Required:** Yes
*   **Response (200 OK):**
```json
{
  "message": "Operation completed successfully",
  "status": 200,
  "data": {
    "id": "user-uuid-12345",
    "name": "Jane Doe",
    "mobileNumber": "9876543210",
    "role": "USER",
    "cohort": "DEFAULT"
  },
  "errors": [],
  "correlationId": "91a629b3-461b-4395-88db-4bfb2875ab9e",
  "timestamp": "2026-05-22T00:55:00.000Z"
}
```

#### 2.2.2 Get Current User Memberships
Retrieves active and historical memberships for the logged-in user.
*   **Endpoint:** `GET /api/v1/me/memberships`
*   **Authentication Required:** Yes
*   **Response (200 OK):**
```json
{
  "message": "Operation completed successfully",
  "status": 200,
  "data": [
    {
      "id": "membership-uuid-999",
      "userId": "user-uuid-12345",
      "userName": "Jane Doe",
      "membershipTierId": "tier-uuid-gold",
      "membershipTierName": "GOLD",
      "membershipPlanId": "plan-uuid-annual",
      "membershipPlanName": "Annual Gold billing Plan",
      "startDate": "2026-01-01T00:00:00.000Z",
      "endDate": "2026-12-31T23:59:59.000Z",
      "status": "ACTIVE",
      "purchasedPrice": 120.00,
      "discountAmount": 20.00,
      "finalPrice": 100.00,
      "autoRenew": true
    }
  ],
  "errors": [],
  "correlationId": "91a629b3-461b-4395-88db-4bfb2875ab9e",
  "timestamp": "2026-05-22T00:55:02.000Z"
}
```

#### 2.2.3 Get Membership Tiers (Paginated)
Retrieves a paginated list of all active membership tiers.
*   **Endpoint:** `GET /api/v1/tiers`
*   **Authentication Required:** Yes
*   **Query Parameters:**
    *   `page`: Page index (0-based, default 0)
    *   `size`: Page size (default 20)
    *   `sort`: Sort property and direction (e.g., `priority,asc`)
*   **Response (200 OK):**
```json
{
  "message": "Operation completed successfully",
  "status": 200,
  "data": {
    "content": [
      {
        "id": "tier-uuid-gold",
        "name": "GOLD",
        "priority": 10,
        "description": "Gold Tier Benefits",
        "isActive": true,
        "version": 1
      }
    ],
    "page": {
      "size": 20,
      "number": 0,
      "totalElements": 1,
      "totalPages": 1
    }
  },
  "errors": [],
  "correlationId": "91a629b3-461b-4395-88db-4bfb2875ab9e",
  "timestamp": "2026-05-22T00:55:05.000Z"
}
```

#### 2.2.4 Get Membership Tier by ID
Retrieves details of a specific membership tier by its ID.
*   **Endpoint:** `GET /api/v1/tiers/{id}`
*   **Authentication Required:** Yes
*   **Response (200 OK):**
```json
{
  "message": "Operation completed successfully",
  "status": 200,
  "data": {
    "id": "tier-uuid-gold",
    "name": "GOLD",
    "priority": 10,
    "description": "Gold Tier Benefits",
    "isActive": true,
    "version": 1
  },
  "errors": [],
  "correlationId": "91a629b3-461b-4395-88db-4bfb2875ab9e",
  "timestamp": "2026-05-22T00:55:08.000Z"
}
```

---

### 2.3 Privileged Admin APIs

These endpoints require the caller to have the role `ADMIN` or `SUPER_ADMIN`.

#### 2.3.1 Create Membership Tier
*   **Endpoint:** `POST /api/v1/admin/tiers`
*   **Authentication Required:** Yes (Role: `ADMIN` or `SUPER_ADMIN`)
*   **Request Body:**
```json
{
  "name": "PLATINUM",
  "priority": 20,
  "description": "Platinum VIP Benefits"
}
```
*   **Response (201 Created):**
```json
{
  "message": "Membership tier created successfully",
  "status": 201,
  "data": {
    "id": "tier-uuid-platinum",
    "name": "PLATINUM",
    "priority": 20,
    "description": "Platinum VIP Benefits",
    "isActive": true,
    "version": 1
  },
  "errors": [],
  "correlationId": "f1d43a6c-d28c-4a37-9831-294b0dcf5799",
  "timestamp": "2026-05-22T00:55:12.000Z"
}
```

#### 2.3.2 Update Membership Tier
*   **Endpoint:** `PUT /api/v1/admin/tiers/{id}`
*   **Authentication Required:** Yes (Role: `ADMIN` or `SUPER_ADMIN`)
*   **Request Body:**
```json
{
  "name": "PLATINUM_PRO",
  "priority": 25,
  "description": "Platinum Pro Benefits",
  "isActive": true,
  "version": 1
}
```
*   **Response (200 OK):**
```json
{
  "message": "Membership tier updated successfully",
  "status": 200,
  "data": {
    "id": "tier-uuid-platinum",
    "name": "PLATINUM_PRO",
    "priority": 25,
    "description": "Platinum Pro Benefits",
    "isActive": true,
    "version": 2
  },
  "errors": [],
  "correlationId": "f1d43a6c-d28c-4a37-9831-294b0dcf5799",
  "timestamp": "2026-05-22T00:55:15.000Z"
}
```

#### 2.3.3 Soft Delete Membership Tier
Deactivates the tier. Cannot delete/deactivate if there are active customer memberships linked to it.
*   **Endpoint:** `DELETE /api/v1/admin/tiers/{id}`
*   **Authentication Required:** Yes (Role: `ADMIN` or `SUPER_ADMIN`)
*   **Response (200 OK):**
```json
{
  "message": "Membership tier deleted successfully",
  "status": 200,
  "data": null,
  "errors": [],
  "correlationId": "f1d43a6c-d28c-4a37-9831-294b0dcf5799",
  "timestamp": "2026-05-22T00:55:18.000Z"
}
```

#### 2.3.4 Create Membership Plan
*   **Endpoint:** `POST /api/v1/admin/plans`
*   **Authentication Required:** Yes (Role: `ADMIN` or `SUPER_ADMIN`)
*   **Request Body:**
```json
{
  "name": "Annual Gold Plan",
  "duration": 12,
  "durationUnit": "MONTHS",
  "basePrice": 120.00,
  "currency": "USD"
}
```
*   **Response (201 Created):**
```json
{
  "message": "Membership plan created successfully",
  "status": 201,
  "data": {
    "id": "plan-uuid-annual",
    "name": "Annual Gold Plan",
    "duration": 12,
    "durationUnit": "MONTHS",
    "basePrice": 120.00,
    "currency": "USD",
    "isActive": true,
    "version": 1
  },
  "errors": [],
  "correlationId": "f1d43a6c-d28c-4a37-9831-294b0dcf5799",
  "timestamp": "2026-05-22T00:55:20.000Z"
}
```

#### 2.3.5 Get All Membership Plans
*   **Endpoint:** `GET /api/v1/admin/plans`
*   **Authentication Required:** Yes (Role: `ADMIN` or `SUPER_ADMIN`)
*   **Response (200 OK):**
```json
{
  "message": "Operation completed successfully",
  "status": 200,
  "data": [
    {
      "id": "plan-uuid-annual",
      "name": "Annual Gold Plan",
      "duration": 12,
      "durationUnit": "MONTHS",
      "basePrice": 120.00,
      "currency": "USD",
      "isActive": true,
      "version": 1
    }
  ],
  "errors": [],
  "correlationId": "f1d43a6c-d28c-4a37-9831-294b0dcf5799",
  "timestamp": "2026-05-22T00:55:22.000Z"
}
```

#### 2.3.6 Get All Customer Memberships
Retrieves all customer memberships globally across all users.
*   **Endpoint:** `GET /api/v1/admin/memberships`
*   **Authentication Required:** Yes (Role: `ADMIN` or `SUPER_ADMIN`)
*   **Response (200 OK):**
```json
{
  "message": "Operation completed successfully",
  "status": 200,
  "data": [
    {
      "id": "membership-uuid-999",
      "userId": "user-uuid-12345",
      "userName": "Jane Doe",
      "membershipTierId": "tier-uuid-gold",
      "membershipTierName": "GOLD",
      "membershipPlanId": "plan-uuid-annual",
      "membershipPlanName": "Annual Gold Plan",
      "startDate": "2026-01-01T00:00:00.000Z",
      "endDate": "2026-12-31T23:59:59.000Z",
      "status": "ACTIVE",
      "purchasedPrice": 120.00,
      "discountAmount": 20.00,
      "finalPrice": 100.00,
      "autoRenew": true
    }
  ],
  "errors": [],
  "correlationId": "f1d43a6c-d28c-4a37-9831-294b0dcf5799",
  "timestamp": "2026-05-22T00:55:25.000Z"
}
```

---

## 3. Request Validation Rules

Prior to controller routing, request payloads are validated. Validation failures return a `400 Bad Request` HTTP status.

### 3.1 Auth Module Validation
*   **Name:** Required (`@NotBlank`), maximum 100 characters (`@Size`).
*   **Mobile Number:** Required (`@NotBlank`), must contain between 10 and 15 digits (`@Pattern`).
*   **Password:** Required (`@NotBlank`), between 3 and 100 characters (`@Size`).

### 3.2 Membership Tier Module Validation
*   **Name:** Required (`@NotBlank`), uppercase alphanumeric (`@Pattern`), between 2 and 50 characters (`@Size`).
*   **Priority:** Required (`@NotNull`), positive integer (`@Min(1)`).
*   **Description:** Optional, maximum 255 characters (`@Size`).

### 3.3 Membership Plan Module Validation
*   **Name:** Required (`@NotBlank`), between 2 and 100 characters (`@Size`).
*   **Duration:** Required (`@NotNull`), positive integer (`@Min(1)`).
*   **Duration Unit:** Required (`@NotBlank`), matching `DAYS`, `WEEKS`, `MONTHS`, or `YEARS`.
*   **Base Price:** Required (`@NotNull`), non-negative decimal (`@DecimalMin("0.0")`).
*   **Currency:** Required (`@NotBlank`), exactly 3 characters (`@Size(min = 3, max = 3)`).
