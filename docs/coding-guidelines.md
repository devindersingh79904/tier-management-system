# Coding Guidelines

This document outlines the coding standards, design patterns, conventions, and quality guidelines to maintain a clean, readable, and highly maintainable codebase for the Spring Boot Loyalty Tier System.

---

## 1. Naming Conventions

Consistency in naming improves readability and reduces cognitive load when navigating the project:

| Type | Case Convention | Example |
| :--- | :--- | :--- |
| **Classes / Interfaces** | PascalCase | `LoyaltyTierService`, `BaseEntity` |
| **Methods** | camelCase | `updateTierPoints()`, `findById()` |
| **Variables / Parameters** | camelCase | `tierId`, `pointsRequirement` |
| **Constants** | UPPER_SNAKE_CASE | `MDC_KEY`, `CORRELATION_HEADER` |
| **Packages** | lowercase | `com.devinder.loyalty.controller` |
| **Database Tables** | snake_case (pluralized) | `loyalty_tiers` |
| **Database Columns** | snake_case | `minimum_points`, `created_at` |

---

## 2. Architecture & Layer Responsibilities

We adhere strictly to the classic **Controller → Service → Repository** layered architecture:

### Controller Layer (`controller`)
*   **Thin Controllers:** Controllers must remain extremely thin. Their sole responsibility is handling HTTP request parsing, routing, validation, and invoking service methods.
*   **No Business Logic:** Absolutely **no business logic** is allowed in the Controller layer.
*   **Return Type:** Controllers must return `ResponseEntity<?>` (specifically using typed envelopes like `ResponseEntity<ApiResponse<DTO>>`).
*   **Validation:** Must enforce early request validation using `@Valid` or `@Validated` on request bodies and parameters.
*   **Response Wrapping:** Standardize all API responses using a centralized response wrapper structure (see `api-standards.md`).

### Service Layer (`service` / `service/impl`)
*   **Core Domain Logic:** All business rules, decision-making logic, calculations, and integrations belong exclusively here.
*   **Transaction Boundaries:** Manage database transactional boundaries at this layer using `@Transactional`.
*   **Service Class Sizes:** Avoid bloated service classes. Extract logical helper classes, builders, or split concerns into smaller services if a service grows too large.

### Repository Layer (`repository`)
*   **Database Operations Only:** Repositories must handle database access, queries, and persistence operations exclusively.
*   **No Business Logic:** Business rules must never leak into repositories.
*   **Data Models:** Interact using JPA entities. Return domain objects to the Service layer.

### Data Transfer Objects (DTOs)
*   **Request & Response Mapping:** Always use DTOs for mapping HTTP requests and responses. Never expose raw JPA database entities directly to the API clients.
*   **Request DTOs (`dto/request/`):** Carry input data with validation annotations. Postfixed with `Request` (e.g., `LoyaltyTierRequest`).
*   **Response DTOs (`dto/response/`):** Represent read-only API payloads. Postfixed with `Response` (e.g., `LoyaltyTierResponse`).

---

## 3. Clean Code & SOLID Principles

*   **DRY (Don't Repeat Yourself):** Avoid duplicate logic. Extract shared calculations, mappings, and utility operations into common utility classes or services.
*   **Single Responsibility Principle (SRP):** Every class, package, and method must have exactly one reason to change. Keep methods small, focused, and readable (ideally under 20-30 lines).
*   **SOLID Principles:** Ensure loosely coupled, extensible components:
    *   **OCP (Open/Closed):** Keep classes closed for modification but open for extension by coding to interfaces.
    *   **LSP (Liskov Substitution):** Subclasses must be substitutable for their base classes.
    *   **ISP (Interface Segregation):** Keep interfaces focused. Avoid fat interfaces.
    *   **DIP (Dependency Inversion):** Depend on abstractions (Interfaces) rather than concrete classes.

---

## 4. API & Validation Standards

*   **Centralized Response Structure:** All APIs must wrap responses inside a unified layout (no raw entity return types, no status code inside the JSON payload envelope to avoid redundancy with the HTTP status code).
*   **HTTP Status Codes:** Map responses to the appropriate HTTP status codes (e.g., `200 OK` for success, `201 Created` for creations, `400 Bad Request` for validation failures, `404 Not Found` for missing resources, `409 Conflict` for state violations).
*   **JSR-380 Validation:** Enforce strict validation rules on request body DTOs:
    *   Use `@NotBlank` for required string fields.
    *   Use `@NotNull` for non-string fields (Integers, Enums, etc.).
    *   Use `@Size` to prevent buffer overflow/long input database failures.
    *   Use `@Min`/`@Max` to enforce range constraints.

---

## 5. Constants & Enums

*   **No Magic Strings:** Avoid inline magic strings and hardcoded literals in controllers, services, and repositories.
*   **Constants Package:** Reusable system-wide constants (e.g., header names, error codes, defaults) must be stored inside a dedicated `constants` package (e.g., `com.devinder.loyalty.constants`).
*   **Prefer Enums:** Use enums instead of raw string values for categorical properties (e.g., subscription statuses like `ACTIVE`, `CANCELLED`; benefit types like `DISCOUNT`, `FREE_SHIPPING`).

---

## 6. Dependency Injection (DI)

*   **Constructor Injection:** Always use constructor injection over field-level `@Autowired`.
*   **Lombok Support:** Use `@RequiredArgsConstructor` to auto-generate constructor parameters for `final` dependency fields.
*   **Benefits:** Constructor injection ensures immutability, safe testing (no reflection needed for mocking), and prevents circular dependency loops at startup.

---

## 7. Logging & Correlation Standards

*   **slf4j Logger:** Use `@Slf4j` Lombok annotations to declare logger instances.
*   **Request Lifecycle Tracing:** Log all incoming requests and outgoing responses including key metadata (correlation ID, path, response status, processing time).
*   **Correlation ID:** Track every request lifecycle with a unique `X-Correlation-Id` mapped through Logback MDC. Pass this ID across downstream calls.
*   **Secrets Filtering:** **Never log sensitive data** such as passwords, auth tokens, credit cards, or raw customer PII.
*   **Appropriate Log Levels:**
    *   `INFO`: High-level business milestones (e.g., membership upgrade processed, transaction committed).
    *   `WARN`: Recoverable issues or input validation failures (e.g., resource not found, invalid tier upgrade request).
    *   `ERROR`: System failures, database connectivity loss, unhandled system runtime errors.
    *   `DEBUG`: High-verbosity developer logs, including database query logs and full payloads in development.

---

## 8. Transaction & Concurrency Management

*   **Transactional Boundaries:** Apply `@Transactional` (from the Spring framework, not JEE) at the **Service implementation layer**. Use `@Transactional(readOnly = true)` for read queries to optimize DB resources.
*   **Optimistic Locking (`@Version`):** Prevent concurrent overwrite issues on loyalty accounts/memberships by adding a `@Version` attribute to JPA entities.
*   **Race Conditions:** Subscription upgrade/downgrade logic must handle concurrent updates gracefully (throwing conflict exceptions) to prevent race conditions and duplicate updates.

---

## 9. Validation & Exception Handling

*   **Centralized Handler:** All exceptions must be handled centrally in a `GlobalExceptionHandler` extending `ResponseEntityExceptionHandler`.
*   **No Leaked Stack Traces:** Never expose Java exception stack traces to the API client. Map raw exceptions to safe, structured error responses.
*   **Structured Errors:** Return field-level validation errors in a standard structured JSON array (e.g., specifying the field name and error message) inside the response envelope.

---

## 10. Environment & Security

*   **Never Hardcode Secrets:** Never commit passwords, URLs, database credentials, or secret keys to git.
*   **Environment Configuration:** Store all database URLs, usernames, passwords, and external ports in a local `.env` file.
*   **`.env` in `.gitignore`:** Ensure the `.env` file is explicitly ignored in git to prevent accidental exposures. Use a `.env.example` file to supply dummy template values.

---

## 11. Code Quality & Formatting

*   **No Unused Imports:** Always clean up unused imports from Java classes before committing.
*   **No Commented-Out/Dead Code:** Remove legacy debug blocks, commented-out logic, and unused test methods.
*   **Clean & Modular Package Structure:** Maintain a clean package structure (`controller`, `service`, `repository`, `model`, `dto`, `exception`, `filter`, `config`, `constants`, `mapper`) to keep dependencies structured and predictable.
*   **MapStruct Guidelines:**
    *   Mappers must declare `componentModel = "spring"`.
    *   Maintain strict mapping configurations without writing manual loops.
