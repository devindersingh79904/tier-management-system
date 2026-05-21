# System Architecture

This document describes the architectural layout, package structure, request lifecycle, and core design patterns of the Loyalty Tier System.

---

## 1. Layered Architecture

The application is built on a clean, decoupled layered architecture, separating concerns across distinct, logical boundaries:

```
                  ┌────────────────────────┐
                  │    HTTP Client / UI    │
                  └───────────┬────────────┘
                              │ HTTP Request (with X-Correlation-ID)
                              ▼
                  ┌────────────────────────┐
                  │   Correlation Filter   │  <-- MDC & ID propagation
                  └───────────┬────────────┘
                              │
                              ▼
                  ┌────────────────────────┐
                  │   Controller Layer     │  <-- Route, Validate, Map to DTO
                  └───────────┬────────────┘
                              │ Request DTO
                              ▼
                  ┌────────────────────────┐
                  │     Service Layer      │  <-- Transaction & Business Logic
                  └───────────┬────────────┘
                              │ Entity
                              ▼
                  ┌────────────────────────┐
                  │    Repository Layer    │  <-- Database CRUD Operations
                  └───────────┬────────────┘
                              │ SQL / JPA
                              ▼
                  ┌────────────────────────┐
                  │  PostgreSQL Database   │  <-- Persistence & Versioning
                  └────────────────────────┘
```

### Layer Responsibilities
*   **Filter Layer:** Captures request metadata, generates/reads correlation IDs, prepares logging MDC, caches payloads, and logs requests/responses.
*   **Controller Layer:** Exposes RESTful endpoints, handles HTTP routing, performs request body validation (using JSR-380 `@Valid`), and translates between HTTP payloads and DTOs.
*   **Service Layer:** Executes transactional business rules, coordinates domain services, handles business invariants, and maps entities back to DTOs for the controller layer.
*   **Repository Layer:** Abstract interface representing CRUD operations on the database utilizing Spring Data JPA.
*   **Database Layer:** Persists state and enforces relational rules (primary keys, uniqueness, optimistic lock version checks).

---

## 2. Package Structure & Responsibilities

The package structure follows a domain-driven, component-based layout for high modularity:

*   `config/`: Infrastructure beans, including OpenAPI specs, JPA auditing config, and environment loading properties (`spring-dotenv` context configuration).
*   `controller/`: REST endpoints exposing tier and benefit actions. Strictly free of direct database or business rules.
*   `dto/`: Request payloads (`dto/request/`) and response payloads (`dto/response/`). Strictly decoupled from entity database annotations.
*   `entity/`: JPA entities mapping to physical database schemas. Extends `BaseEntity` for shared columns.
*   `repository/`: Spring Data JPA interfaces representing database operations.
*   `service/` & `service/impl/`: Business logic interfaces and concrete implementations. Defines transactional boundaries (`@Transactional`).
*   `mapper/`: MapStruct mapper interfaces translating entities to DTOs and vice-versa, compiled to native Java bytecode.
*   `exception/`: Centralized domain exception classes and `GlobalExceptionHandler` mapping throwables to client responses.
*   `filter/`: Low-level servlet filters handling request intercepting, Correlation IDs, and logging wrappers.
*   `constants/`: System constants (e.g., standard error codes, header names, message descriptions).
*   `util/`: General helper utilities (e.g., ThreadLocal context for Correlation ID tracking).

---

## 3. Request Lifecycle & DTO Flow

1.  **Request Ingestion:** The client calls an endpoint. The servlet container wraps the request/response streams.
2.  **Filter Interception:**
    *   `CorrelationIdFilter` intercepts the request.
    *   Retrieves `X-Correlation-Id` from request headers or generates a new `UUID`.
    *   Places this ID in the Logging MDC (`MDC.put()`) and ThreadLocal context.
    *   Wraps raw streams in caching wrappers (`ContentCachingRequestWrapper`/`ContentCachingResponseWrapper`) so request/response payloads can be read multiple times without consuming the servlet stream.
3.  **Controller Routing:**
    *   The `DispatcherServlet` routes the request to the matching controller method.
    *   JSR-380 validation is executed on the incoming `@RequestBody DTO` automatically.
    *   If validation fails, a `MethodArgumentNotValidException` is thrown, caught by `GlobalExceptionHandler`, and mapped to a `400 Bad Request` payload.
    *   The controller delegates request parsing to the `Mapper` (MapStruct), transforming the request DTO to an entity.
4.  **Service Processing:**
    *   The service method is executed under a transaction context (`@Transactional`).
    *   Business logic updates the entity state.
    *   If an optimistic lock conflict occurs (on save), JPA throws an `ObjectOptimisticLockingFailureException`.
    *   The service maps the modified entity state back to a response DTO.
5.  **Controller Response:**
    *   The controller wraps the response DTO inside the unified success wrapper (`ApiResponse.success(data)`).
    *   The framework serializes this object to JSON.
6.  **Filter Outgestion:**
    *   The filter logs the request details and status. It captures the response payload from the caching wrapper and outputs it at the `DEBUG` log level.
    *   The response wrapper copies its cached buffer back to the original response stream (`responseWrapper.copyBodyToResponse()`).
    *   MDC context is fully cleared (`MDC.clear()`).

---

## 4. Response Wrapper Strategy

To present a consistent API contract, all API responses (both success and errors) use a single standard envelope format:

```json
{
  "message": "Operation completed successfully.",
  "data": { ... },
  "errors": [],
  "correlationId": "8f409481-050e-433a-bfb7-8488bbf1f4ff",
  "timestamp": "2026-05-21T19:37:02.257"
}
```

*   **Trade-off (No "status" inside body):** HTTP status codes are already supplied in the HTTP header (e.g., `200 OK`, `400 Bad Request`, `409 Conflict`). Including a duplicate `"status": 200` field in the JSON body is redundant and introduces double-maintenance. Instead, our body focuses solely on domain payload and error codes, relying on HTTP headers for transport-level status.

---

## 5. Tracing & Correlation

In a production environment, requests span multiple services, logs, database queries, and background processes. 
*   **The Problem:** Without tracing, locating logs for a single user transaction in a multi-tenant, high-throughput environment is like looking for a needle in a haystack.
*   **The Solution:** Using a **Correlation ID** propagates a single unique request token across filters, logs (via Logback MDC), response headers, database operations, and outbound system API requests. This provides a unified thread of continuity across isolated log messages.

---

## 6. Concurrency Control

Subscription and loyalty systems require strict integrity checks.
*   **The Problem:** If two requests update the same subscription simultaneously (e.g., two parallel API requests updating a member's points balance), the system could suffer database corruption or double-point adjustments.
*   **The Solution:** Optimistic Locking. Every entity contains a `@Version` field. If Transaction B attempts to save changes on an entity read before Transaction A completed its save, Hibernate detects the mismatch and aborts Transaction B with an `ObjectOptimisticLockingFailureException`. This ensures zero concurrency loss without locking databases aggressively.

---

## 7. Overengineering Guardrails

To maintain high development velocity and clear code paths:
*   **Zero-Config Environment Binding:** We use `spring-dotenv` instead of complex, custom properties-parsing code or custom Bootstrap beans.
*   **Simple MapStruct Mappings:** Mapping code is generated at compile time. No heavy reflection-based runtime mappers are allowed.
*   **No Generic Repositories:** Use direct Spring Data interfaces rather than writing redundant wrapper classes (DAO patterns) over repositories.
