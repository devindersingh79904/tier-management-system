# Setup and Execution Guide

This document describes how to configure, build, run, and test the Loyalty Tier System application in a local development environment.

---

## 1. Environment Configurations (.env Strategy)

We use environment variables to manage configuration values. Real passwords and API secrets must **never** be hardcoded in `application.properties` or checked into version control.

### Setup Configurations
1.  Locate the `.env.example` template at the root of the project.
2.  Copy it to create a `.env` file (which is gitignored):
    ```bash
    cp .env.example .env
    ```
3.  Open the newly created `.env` file and configure it with your database credentials.

### Configuration Template (.env.example vs .env)
The `.env.example` file contains safe fallback configurations for a local setup:

```properties
# Server configuration
SERVER_PORT=8080

# PostgreSQL Connection settings
DB_URL=jdbc:postgresql://localhost:5432/loyalty_db
DB_USERNAME=postgres
DB_PASSWORD=password

# JPA / Hibernate configuration
DB_DDL_AUTO=update
DB_SHOW_SQL=true
DB_FORMAT_SQL=true
DB_LOB_NON_CONTEXTUAL_CREATION=true
DB_POOL_SIZE=5
```

> [!IMPORTANT]
> **Production vs. Local Development (`DB_DDL_AUTO`):**
> *   For local development/testing, set `DB_DDL_AUTO=create` or `update` to automatically generate the database schema.
> *   In production, set `DB_DDL_AUTO=validate` or `none` and use a migration tool like Liquibase or Flyway to manage database changes.

---

## 2. Running the Application Locally

The `spring-dotenv` dependency automatically loads the key-value pairs from your `.env` file into Spring's environment properties at startup.

### Run with Maven Wrapper
To build the project and start the server:
```bash
./mvnw clean spring-boot:run -DskipTests
```
*   **Server port:** Defaults to `8080` unless modified via `SERVER_PORT` in your `.env`.
*   **Startup Verification:** You should see the following logs indicating a successful startup:
    ```
    [INFO] HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@...
    [INFO] Tomcat started on port 8080 (http) with context path '/'
    [INFO] Started LoyaltyTierSystemApplication in X.XXX seconds
    ```

---

## 3. Running Tests

To run the test suite:
```bash
./mvnw clean test
```
The test execution automatically boots the Spring context and connects to the database specified in your `.env` file (using the automatic `.env` reader).

---

## 4. OpenAPI & Interactive API Playground

The project integrates Swagger/OpenAPI for interactive documentation.

*   **Swagger UI Interactive Playground:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
*   **Raw OpenAPI Specification JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

You can use the Swagger UI playground to test API endpoints directly from your browser.

---

## 5. Testing the APIs (Manual Verification)

Below is an example flow using `curl` to test the loyalty tier endpoints.

### 1. Create a Loyalty Tier
```bash
curl -X POST http://localhost:8080/api/v1/loyalty-tiers \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: test-create-001" \
  -d '{
    "name": "Platinum Tier",
    "tierLevel": "PLATINUM",
    "minimumPoints": 5000,
    "description": "Top-tier benefits with maximum rewards."
  }'
```

**Expected Response (`201 Created`):**
```json
{
  "message": "Loyalty tier created successfully",
  "data": {
    "id": 1,
    "name": "Platinum Tier",
    "tierLevel": "PLATINUM",
    "minimumPoints": 5000,
    "description": "Top-tier benefits with maximum rewards.",
    "version": 0
  },
  "errors": [],
  "correlationId": "test-create-001",
  "timestamp": "2026-05-21T19:40:00.000Z"
}
```

### 2. Retrieve All Loyalty Tiers
```bash
curl -X GET http://localhost:8080/api/v1/loyalty-tiers \
  -H "X-Correlation-Id: test-get-all"
```

**Expected Response (`200 OK`):**
```json
{
  "message": "Resource retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Platinum Tier",
      "tierLevel": "PLATINUM",
      "minimumPoints": 5000,
      "description": "Top-tier benefits with maximum rewards.",
      "version": 0
    }
  ],
  "errors": [],
  "correlationId": "test-get-all",
  "timestamp": "2026-05-21T19:40:10.000Z"
}
```
