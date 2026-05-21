# API Standards

This document specifies the REST API design conventions, response wrapper format, HTTP status mapping, and payload guidelines for the Loyalty Tier System.

---

## 1. REST Naming Conventions

All endpoints must be resource-oriented and conform to these REST rules:

*   **Plural Nouns:** Path fragments must be plural nouns representing collections:
    *   `GET /api/v1/loyalty-tiers` (Correct)
    *   `GET /api/v1/loyalty-tier` (Incorrect)
*   **Kebab-Case Paths:** URL paths must use lowercase kebab-case naming:
    *   `/api/v1/loyalty-tiers` (Correct)
    *   `/api/v1/loyaltyTiers` or `/api/v1/loyalty_tiers` (Incorrect)
*   **HTTP Methods:** Match operations directly to their standard HTTP methods:
    *   `POST`: Create a resource.
    *   `GET`: Retrieve resources.
    *   `PUT`: Update a resource (idempotent replacement).
    *   `PATCH`: Partial updates.
    *   `DELETE`: Remove a resource.

---

## 2. API Response Envelope

Every response returned by the API—whether successful or failing—must use a unified JSON payload structure. This establishes a clean and predictable API contract.

### Envelope Structure
```json
{
  "message": "Human readable description of the status or outcome.",
  "data": { ... },
  "errors": [ ... ],
  "correlationId": "8f409481-050e-433a-bfb7-8488bbf1f4ff",
  "timestamp": "2026-05-21T19:37:02.257Z"
}
```

*   **`message`:** Short explanation of the result (e.g. `"Operation completed successfully"`).
*   **`data`:** The actual response payload (object or array). Returns `null` on errors.
*   **`errors`:** A list of specific error details (mostly used for validation failures). Empty on success.
*   **`correlationId`:** The unique request ID traced in the logs. Use this when debugging issues.
*   **`timestamp`:** UTC timestamp of the request execution.

> [!IMPORTANT]
> **No Status Code in Body:** Do **not** include fields like `"status": "SUCCESS"` or `"statusCode": 200` in the JSON body. The actual HTTP header status code (e.g. `200`, `400`, `409`, `404`) is the single source of truth. Duplicating it in the JSON body is redundant and leads to code inconsistency.

---

## 3. Success & Error Examples

### Success Example (`GET /api/v1/loyalty-tiers/1`)
**HTTP Status:** `200 OK`
```json
{
  "message": "Resource retrieved successfully",
  "data": {
    "id": 1,
    "name": "Gold Tier",
    "tierLevel": "GOLD",
    "minimumPoints": 1000,
    "description": "Premium tier offering double points on purchases.",
    "version": 1
  },
  "errors": [],
  "correlationId": "d56067fa-f3ce-405a-a2a2-3c2f5f9796b8",
  "timestamp": "2026-05-21T19:37:02.237Z"
}
```

### Business/Validation Error Example (`POST /api/v1/loyalty-tiers` with invalid payload)
**HTTP Status:** `400 Bad Request`
```json
{
  "message": "Validation failed.",
  "data": null,
  "errors": [
    "name: size must be between 2 and 50",
    "minimumPoints: must be greater than or equal to 0"
  ],
  "correlationId": "7164cb8b-6817-4a72-91dc-a1b6d22661f3",
  "timestamp": "2026-05-21T19:37:02.283Z"
}
```

### Business Conflict Error Example (`POST /api/v1/loyalty-tiers` with duplicate name)
**HTTP Status:** `409 Conflict`
```json
{
  "message": "A loyalty tier with the name 'Gold Tier' already exists.",
  "data": null,
  "errors": [
    "ERR_CONFLICT"
  ],
  "correlationId": "4be84fd3-e0cd-4a04-8d90-6742d50195fd",
  "timestamp": "2026-05-21T19:37:10.986Z"
}
```

### Unexpected System Error Example (Unhandled Exception)
**HTTP Status:** `500 Internal Server Error`
```json
{
  "message": "An unexpected error occurred. Please contact system support.",
  "data": null,
  "errors": [
    "ERR_SYSTEM_ERROR"
  ],
  "correlationId": "c60ec9a2-5dc9-45c3-9f6d-3b295ba0243b",
  "timestamp": "2026-05-21T19:37:10.970Z"
}
```

---

## 4. HTTP Status Code Mapping

Use the standard HTTP status codes correctly. Do not override HTTP status meanings:

| Status Code | Usage in System |
| :--- | :--- |
| **`200 OK`** | Successful retrieval, resource update, or generic success. |
| **`201 Created`** | Successful creation of a resource (e.g. creating a tier). |
| **`400 Bad Request`** | Input validation failure, malformed JSON, or syntactic request errors. |
| **`404 Not Found`** | Resource target (e.g. tier by ID) does not exist. |
| **`409 Conflict`** | Resource unique constraints violated (e.g. duplicate name) or optimistic locking checks failed. |
| **`500 Internal Server Error`** | Unhandled system exception, database connection loss, or infrastructure outage. |

---

## 5. Header Propagation (X-Correlation-Id)

*   All client API requests should ideally pass an `X-Correlation-Id` header containing a unique tracking string.
*   If the client does not send this header, the backend generated UUID is applied.
*   The API **always** returns the tracking ID in the response headers as:
    `X-Correlation-Id: <uuid>`
    allowing immediate tracing if an issue is reported by frontend clients or external consuming systems.
