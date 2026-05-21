# Logging and Correlation Tracing

This document describes how correlation IDs are generated, propagated, and utilized across requests to build a production-grade tracing system.

---

## 1. Tracing Request Lifecycle (X-Correlation-Id Flow)

To map log entries to specific API invocations, we run a low-level servlet filter that intercepts all incoming traffic:

```
[Client Request]
       │
       ▼
[CorrelationIdFilter]
       │  1. Check headers for "X-Correlation-Id".
       │  2. If missing, generate new UUID.
       │  3. Register in MDC (Mapped Diagnostic Context).
       │  4. Set "X-Correlation-Id" header in HTTP response.
       ▼
[Application Context]
       │  - Core controller/service/repository code executes.
       │  - Any log.info() or log.error() automatically prints the ID.
       ▼
[Filter Response Tear-Down]
          - Calculate request execution duration.
          - Log final HTTP status and duration.
          - Clear MDC (prevent thread-local leak).
```

---

## 2. Mapped Diagnostic Context (MDC) & Logback Integration

Logging statements within a high-concurrency server can become intertwined. Without context, matching a service error to a database query is difficult.

### How MDC Works
MDC uses a thread-local map managed by the logging framework. When a value is placed in the MDC (e.g. `MDC.put("correlationId", id)`), any logging statement executed on that thread will automatically print the value if the logger layout is configured to output it.

### Logback Console Layout Configuration (`logback-spring.xml`)
The console pattern includes the custom `%X{correlationId}` placeholder:

```xml
<property name="CONSOLE_LOG_PATTERN" 
          value="%clr([%d{yyyy-MM-dd HH:mm:ss.SSS}]){faint} %clr([%X{correlationId:-no-cid}]){magenta} %clr([%-5p]){yellow} %clr(%logger{39}){cyan} - %m%n"/>
```

*   If a request context exists, the correlation ID is colored in magenta (e.g. `[8f409481-...]`).
*   If a log statement occurs outside of a request context (like background JVM tasks or startup routines), it defaults to `[no-cid]`.

---

## 3. Request/Response Body Payload Logging

To capture incoming parameters and response payloads without breaking standard servlet streams, we implement caching wrappers:

*   **The Problem:** The standard Servlet request/response input streams can only be read once. Reading the request body in a filter to log it would consume the stream, preventing controllers from binding it to DTOs.
*   **The Solution:** We wrap the `HttpServletRequest` and `HttpServletResponse` in Spring utility caching wrappers:
    *   `ContentCachingRequestWrapper`
    *   `ContentCachingResponseWrapper`
*   These wrappers cache the content as it is read or written. During response tear-down in the filter, the cached content is safely retrieved and written to the logs.
*   Finally, `responseWrapper.copyBodyToResponse()` is executed to write the cached payload back to the real response channel, ensuring the client receives the data.

---

## 4. Log Level Standards

To prevent disk-space exhaustion and performance degradation on production servers, logs must use correct severity levels:

*   **`INFO` Level:** Used for general request logging (e.g., `Incoming Request: GET /api/v1/loyalty-tiers` and `Outgoing Response: 200 /api/v1/loyalty-tiers (12ms)`).
*   **`DEBUG` Level:** Used to log detailed request/response body payloads. Because these payloads can be large and contain repetitive structure, logging them under `INFO` is too noisy for high-traffic environments. Developers can enable `DEBUG` logs temporarily in non-production environments to audit payloads.
*   **`WARN` Level:** Used to log recoverable business validation errors or expected database constraint failures. These do not represent system degradation but are helpful for diagnostic purposes.
*   **`ERROR` Level:** Used to log infrastructure errors (connection failures, out-of-memory errors, system bugs) accompanied by full stack traces.

---

## 5. Security & Masking Sensitive Data

> [!WARNING]
> **Do Not Log Sensitive Information:** 
> Logging PII (Personally Identifiable Information) such as user passwords, credit card numbers, authentication tokens, or personal identifiers violates security compliance laws (e.g., PCI-DSS, GDPR, HIPAA).
> Ensure that logging filters avoid printing payloads from sensitive authentication endpoints or implement regex masking for fields like `password`, `token`, or `cvv`.
