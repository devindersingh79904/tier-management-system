# Loyalty Tier System

The **Loyalty Tier System** is a production-grade Spring Boot 3 application designed to manage subscription plans, loyalty tiers, configurable benefits, and membership lifecycles.

This project implements a highly scalable, concurrency-safe, and audit-friendly JPA entity layer designed for high-performance enterprise applications.

---

## Features & Architecture

- **Clean & Modular Design:** Standardized package layout (`config`, `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `exception`, `filter`, `enums`, `constants`, `util`).
- **JPA Entity Layer (9 Core Tables):**
  - `users`: Authenticated users uniquely identified by `mobileNumber`. Passwords are encrypted using BCrypt (never stored in plain text).
  - `membership_tiers`: Dynamic, admin-configurable loyalty levels (e.g. `SILVER`, `GOLD`, `PLATINUM`) using a sorting `priority`.
  - `membership_plans`: Billing models (Monthly, Quarterly, Yearly) with prices stored in cents/paise (`Long`) to prevent floating-point errors.
  - `user_memberships`: Links users to plans and tiers with active statuses and subscription dates.
  - `membership_benefits`: A catalog of available perks (e.g. `FREE_DELIVERY`, `PRIORITY_SUPPORT`).
  - `benefit_configurations`: Dynamic configuration mappings of benefits to plans/tiers using PostgreSQL `jsonb`.
  - `tier_criteria`: Flexible JSONB rule sets defining criteria required to qualify for loyalty tiers.
  - `membership_events`: Append-only, historical audit trail tracking events (`SUBSCRIBED`, `RENEWED`, `UPGRADED`, etc.).
  - `payment_intents`: Payment intent records tracking idempotency keys, providers, and transaction statuses.
- **Lazy Loading Strategy:** All `ManyToOne` relationships are configured with `FetchType.LAZY` to avoid N+1 query loops.
- **JPA Auditing & Optimistic Locking:** All entities inherit from `BaseEntity` which exposes:
    - `id` (String) - standard UUID string generated via standard JPA 3.1 `GenerationType.UUID`.
    - `createdAt` and `updatedAt` - managed automatically via `@EntityListeners(AuditingEntityListener.class)`.
    - `version` - `@Version` optimistic locking to prevent concurrency race conditions on active memberships.
- **Index-Optimized Performance:** Optimized database performance using explicit table indexes (e.g., UNIQUE index on `mobile_number`, UNIQUE index on `idempotency_key`, index on event `created_at`, index on `transaction_id`, etc.).

---

## Setup Instructions

### Prerequisites
- **Java Development Kit (JDK) 21** or higher
- **Maven** (packaged wrapper `./mvnw`)
- **PostgreSQL** (version 15+ recommended for native `jsonb`)

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
   Open the `.env` file and input your local or remote database URL, username, and password.

---

## Execution Instructions

### Running Locally
To compile the codebase and run the Spring Boot application locally:
1. Ensure your PostgreSQL database is running or you have access to a remote database (e.g. Neon).
2. Configure your database credentials in `.env` (this file is excluded from git control via `.gitignore`).
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Running Tests
To run the automated JUnit test suite (which validates JPA entity mappings and compiles all files):
```bash
./mvnw clean test
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

# Seeding configuration
export GLOBAL_SEED_ENABLED=true
export USER_SEED_ENABLED=true
export TIER_SEED_ENABLED=true
export PLAN_SEED_ENABLED=true
export BENEFIT_SEED_ENABLED=true
export TIER_CRITERIA_SEED_ENABLED=true
export BENEFIT_CONFIGURATION_SEED_ENABLED=true
export USER_MEMBERSHIP_SEED_ENABLED=true
export PAYMENT_INTENT_SEED_ENABLED=true
```

---

## Detailed Documentation
Refer to the following detailed guides for design decisions, standards, and architecture:
- [Setup & Configuration Guide](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/setup-guide.md)
- [Database Design & Schema](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/database-design.md)
- [Database Seeding Strategy](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/seeding-strategy.md)
- [Architecture & Layers](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/architecture.md)
- [API Design Standards](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/api-standards.md)
- [Concurrency & Locking](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/concurrency.md)
- [Exception Handling Policy](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/exception-handling.md)
- [Logging & Correlation Id Filtering](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/logging-and-correlation.md)

