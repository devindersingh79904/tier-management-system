# Loyalty Tier System

The **Loyalty Tier System** is a production-grade Spring Boot 3 application designed to manage subscription plans, loyalty tiers, configurable benefits, and membership lifecycles.

This project implements a highly scalable, concurrency-safe, and audit-friendly JPA entity layer designed for high-performance enterprise applications.

---

## Features & Architecture

- **Clean & Modular Design:** Standardized package layout (`config`, `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `exception`, `filter`, `enums`, `constants`, `util`).
- **Stateless Authentication & Authorization:** Implements stateless session management using Spring Security 6 and JSON Web Tokens (JWT). Supports roles (`USER`, `ADMIN`, `SUPER_ADMIN`) and secure route-based and method-level access control.
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
- **Index-Optimized Performance:** Optimized database performance using explicit table indexes (e.g., UNIQUE index on `mobile_number`, UNIQUE index on `idempotency_key`, index on event `created_at`, index on `transaction_id`, and indexes on `membership_tiers`' `priority` and `is_active` fields).
- **Membership Tier Management Module:** End-to-end management of loyalty tiers:
  - Create, read, update, and soft delete membership tiers.
  - Programmatic uniqueness validation for tier `name` and `priority` globally (throwing `409 Conflict` on duplicates).
  - Validation check preventing deletion or deactivation of any tier that has active customer memberships (`status = ACTIVE`), throwing a `409 Conflict` with the message `"Cannot deactivate tier because active memberships exist."`.
  - Endpoint-level security requiring `@PreAuthorize("isAuthenticated()")` for user-access GET endpoints, and `@PreAuthorize("hasRole('ADMIN')")` for create, update, and delete endpoints.
  - Complete OpenAPI swagger schema polish using `@Schema` annotations on all tier requests and response DTOs.

---

## API Endpoints

The Loyalty Tier System REST APIs are partitioned into Public, User Self-Service, and Privileged Admin groups:

### Public / Authentication APIs
| Method | Endpoint | Access Control | Description |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/v1/auth/signup` | Public / Unauthenticated | Create a new user profile with default `USER` role. |
| **POST** | `/api/v1/auth/login` | Public / Unauthenticated | Authenticate mobile/password credentials and return JWTs. |

### User Self-Service APIs
| Method | Endpoint | Access Control | Description |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/v1/me/profile` | `isAuthenticated()` | Retrieve details of the currently authenticated user. |
| **GET** | `/api/v1/me/memberships` | `isAuthenticated()` | Retrieve the active membership for the logged-in user context. |
| **POST** | `/api/v1/me/memberships` | `isAuthenticated()` | Customer self-subscription to an active plan & tier. |
| **GET** | `/api/v1/me/memberships/history` | `isAuthenticated()` | Retrieve full membership history of the authenticated user. |
| **GET** | `/api/v1/me/memberships/benefits` | `isAuthenticated()` | Dynamically resolve and merge active benefit configurations for the user. |
| **GET** | `/api/v1/plans` | `isAuthenticated()` | List active membership plans paginated. |
| **GET** | `/api/v1/plans/{id}` | `isAuthenticated()` | Retrieve a specific active membership plan by ID. |
| **GET** | `/api/v1/tiers` | `isAuthenticated()` | List active membership tiers paginated. |
| **GET** | `/api/v1/tiers/{id}` | `isAuthenticated()` | Retrieve a specific membership tier by UUID. |

### Privileged Admin APIs (Required Role: `ADMIN` or `SUPER_ADMIN`)

#### 1. Membership Tiers
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/v1/admin/tiers` | Create a new membership tier. Requires unique name/priority. |
| **PUT** | `/api/v1/admin/tiers/{id}` | Update details of a tier. Enforces uniqueness. |
| **DELETE** | `/api/v1/admin/tiers/{id}` | Soft delete/deactivate a tier. Blocked if active memberships exist. |
| **GET** | `/api/v1/admin/tiers` | List all membership tiers (active & inactive). |
| **GET** | `/api/v1/admin/tiers/{id}` | Retrieve a specific membership tier details. |

#### 2. Membership Plans
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/v1/admin/plans` | Create a new billing plan. Base prices stored in paise/cents (`Long`). |
| **PUT** | `/api/v1/admin/plans/{id}` | Update details of a plan. |
| **DELETE** | `/api/v1/admin/plans/{id}` | Soft delete/deactivate a plan. Blocked if active memberships exist. |
| **GET** | `/api/v1/admin/plans` | List all billing plans (active & inactive). |
| **GET** | `/api/v1/admin/plans/{id}` | Retrieve a specific billing plan. |

#### 3. User Memberships
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/v1/admin/memberships` | Manually register a membership for any user. Checks active status of plan/tier. |
| **GET** | `/api/v1/admin/memberships` | List all user membership records in the system (paginated). |
| **GET** | `/api/v1/admin/memberships/{id}` | Retrieve detailed membership information. |
| **PUT** | `/api/v1/admin/memberships/{id}/upgrade` | Upgrade an active membership to a higher priority tier. |
| **PUT** | `/api/v1/admin/memberships/{id}/cancel` | Cancel an active membership and log audit details. |

#### 4. Membership Benefits Catalog
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/v1/admin/benefits` | Create a new benefit perk (e.g., `FREE_SHIPPING`, `DISCOUNT`). |
| **PUT** | `/api/v1/admin/benefits/{id}` | Update benefit perk details. |
| **DELETE** | `/api/v1/admin/benefits/{id}` | Soft delete/deactivate a benefit perk. |
| **GET** | `/api/v1/admin/benefits` | List all catalog benefit perks. |
| **GET** | `/api/v1/admin/benefits/{id}` | Retrieve specific benefit details. |

#### 5. Benefit Configurations
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/v1/admin/benefit-configurations` | Define custom plan/tier configs for a benefit using native PostgreSQL JSONB. |
| **PUT** | `/api/v1/admin/benefit-configurations/{id}` | Update JSONB configuration details. Enforces JSON format verification. |
| **DELETE** | `/api/v1/admin/benefit-configurations/{id}` | Soft delete/deactivate benefit configuration. |
| **GET** | `/api/v1/admin/benefit-configurations` | List all benefit configurations. |
| **GET** | `/api/v1/admin/benefit-configurations/{id}` | Retrieve specific configuration details. |

#### 6. Tier Eligibility Criteria
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/v1/admin/tier-criteria` | Define eligibility JSONB criteria rules (numeric/string comparisons, logical AND/OR). |
| **PUT** | `/api/v1/admin/tier-criteria/{id}` | Update eligibility criteria rules. Enforces JSON format validation. |
| **DELETE** | `/api/v1/admin/tier-criteria/{id}` | Soft delete/deactivate tier criteria. |
| **GET** | `/api/v1/admin/tier-criteria` | List all tier criteria rules. |
| **GET** | `/api/v1/admin/tier-criteria/{id}` | Retrieve specific tier criteria rules. |


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

# Seeding configuration (Enabling global seeding will automatically clear the target tables before populating fresh data)
export GLOBAL_SEED_ENABLED=true
export USER_SEED_ENABLED=true
export TIER_SEED_ENABLED=true
export PLAN_SEED_ENABLED=true
export BENEFIT_SEED_ENABLED=true
export TIER_CRITERIA_SEED_ENABLED=true
export BENEFIT_CONFIGURATION_SEED_ENABLED=true
export USER_MEMBERSHIP_SEED_ENABLED=true
export PAYMENT_INTENT_SEED_ENABLED=true

# JWT Token configuration
export JWT_SECRET=9a4f2c8d3b7a1e5f8c6b2a1d0f9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e
export ACCESS_TOKEN_EXPIRY_MINUTES=300
export REFRESH_TOKEN_EXPIRY_DAYS=30
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
- [Security Architecture](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/security-architecture.md)
- [JWT Token Strategy](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/jwt-strategy.md)
- [Authentication flow](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/authentication-flow.md)
- [API Guidelines - Authentication & Authorization](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/api-guidelines.md)
- [Role Access Matrix](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/role-access-matrix.md)
- [Security Guide](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/security-guide.md)


