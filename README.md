# Loyalty Tier System

The **Loyalty Tier System** is a production-grade Spring Boot 3 application designed to manage subscription plans, loyalty tiers, configurable benefits, and membership lifecycles.

## Features & Architecture
- **Clean & Modular Design:** Standardized packages (config, controller, service/impl, repository, entity, dto, mapper, exception, filter).
- **Extensible Base Entity:** Audited entities with `@CreatedDate`, `@LastModifiedDate`, and `@Version` supporting Optimistic Locking to prevent concurrent subscription update conflicts.
- **Correlation ID Logging & Tracing:** Implements a servlet filter tracking `X-Correlation-Id` across requests, propagating it via logging MDC and response wrappers.
- **Unified API Response Wrapper:** Consistent response structure for success and error scenarios.
- **Global Exception Handling:** Custom exceptions mapping to correct HTTP statuses.

---

## Setup Instructions

### Prerequisites
- **Java Development Kit (JDK) 21** or higher
- **Maven** (packaged wrapper `./mvnw`)
- **PostgreSQL**

### Installation
1. Clone the repository:
   ```bash
   git clone git@github.com:devindersingh79904/tier-management-system.git
   cd loyalty-tier-system
   ```

2. Set up environment variables:
   Copy the example environment template:
   ```bash
   cp .env.example .env
   ```

---

## Execution Instructions

### Running Locally
To connect to the PostgreSQL instance:
1. Ensure your PostgreSQL database is running or you have access to a remote database (e.g. Neon).
2. Configure your database credentials in `.env` (this file is excluded from git control via `.gitignore`).
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Running Tests
To run the test suite:
```bash
./mvnw test
```

### Packaging the Application
To compile, test, and package the application into a runnable fat JAR:
```bash
./mvnw clean package
```

---

## OpenAPI & Swagger Documentation
Once the server is running, you can explore, test, and interact with the REST APIs via Swagger UI:
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI Description:** `http://localhost:8080/v3/api-docs`

---

## Environment Variables Example
Below is an example of configurations that can be populated in `.env` or exported in the host environment (replace with your actual database URL and credentials):

```bash
# Server configuration
export SERVER_PORT=8080

# PostgreSQL Connection settings
export DB_URL=jdbc:postgresql://localhost:5432/loyalty_db
export DB_USERNAME=postgres
export DB_PASSWORD=password

# JPA / Hibernate configuration
export DB_DDL_AUTO=update
export DB_SHOW_SQL=true
export DB_FORMAT_SQL=true
export DB_LOB_NON_CONTEXTUAL_CREATION=true
export DB_POOL_SIZE=5
```
