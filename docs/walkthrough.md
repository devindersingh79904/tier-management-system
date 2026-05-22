# Walkthrough - Loyalty Tier System Modules 3 - 7 Implementation

This document provides a comprehensive technical walkthrough of the end-to-end implementation for **Modules 3 to 7** of the **Loyalty Tier System** backend. It describes the architecture, dynamic JSON engines, business logic validations, security boundaries, and validation test results.

---

## 1. Module 3: Membership Plans
We implemented administrative management of billing plans and customer-facing read-only APIs:
- **Administrative CRUD:** Allowed complete control over membership plans via `/api/v1/admin/plans`. Protected via `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")`.
- **Soft Delete Policy:** When a plan is deleted, it is soft-deleted by setting `isActive = false`. We enforce that a plan cannot be deleted or deactivated if it has any active customer memberships, preventing service disruption.
- **Financial Precision:** Encapsulated plan base prices inside `Long` values representing paise/cents (e.g. ₹49.00 = `4900L`) to prevent floating-point calculation errors.
- **User APIs:** Exposed `/api/v1/plans` and `/api/v1/plans/{id}` to allow authenticated users to browse available active subscription options.

---

## 2. Module 4: User Memberships
We built a production-grade membership subscription lifecycle engine including subscriptions, upgrades, cancellations, and auditing:
- **Natural Idempotency:** We enforce that a customer cannot have overlapping active memberships. If they attempt to self-subscribe while having an active membership, a `ConflictException` is thrown.
- **State Integrity Validations:** During subscription creation (both Admin and User self-service), the target MembershipPlan and MembershipTier must be active (`isActive = true`). Subscribing to inactive configurations triggers a conflict error.
- **Upgrades Engine:** Upgrades enforce that the target tier has a strictly higher sorting `priority` than the current active tier. Final prices and dates are recalculated dynamically.
- **Auditing Trail:** Every transition is tracked by automatically logging transactional details into the `membership_events` table (`SUBSCRIBED`, `UPGRADED`, `CANCELLED`, `EXPIRED`, `RENEWED`).
- **Scheduled Job Service Hook:** Added a background expiration method `expireMemberships()` that automatically transitions all memberships past their `endDate` from `ACTIVE` to `EXPIRED`.
- **Transaction Boundaries:** Enforced `@Transactional` boundaries on all membership write, upgrade, and cancellation service flows.

---

## 3. Module 5: Membership Benefits
We built a dynamic perks catalog to configure loyalty benefit traits:
- **Perks Catalog:** Managed catalog items through `/api/v1/admin/benefits` mapping to the `membership_benefits` database schema.
- **Soft Deactivation:** Deleting a benefit sets `isActive = false` rather than physically removing the database record to preserve historical transaction relations.
- **Constraint Checks:** Enforced global uniqueness on the benefit `name` field to prevent duplicate configurations.

---

## 4. Module 6: Benefit Configurations & Resolution Engine
We created an rules engine to assign benefit settings to plans and tiers and resolve them dynamically:
- **Schema Mapping:** Built configurations mapping specific plans, tiers, or plan-tier intersections to benefit layouts. Configurations are stored natively in PostgreSQL `jsonb` fields.
- **Jackson Parsing & Validation:** Centralized all Jackson JSON serialization and format checking within a thread-safe `JsonValidationUtil` class, throwing `BadRequestException` on malformed request shapes.
- **Resolution and Merging Engine:** Implemented a recursive resolving engine (`BenefitResolverService`) that fetches all active benefit configurations matching a user's current plan and tier. When configurations overlap, the system recursively parses and merges the JSON structures, prioritizing plan-tier intersections over standalone definitions.

---

## 5. Module 7: Tier Criteria Rules Engine
We implemented a dynamic, AST-like evaluation rules engine to evaluate silver, gold, and platinum loyalty tier eligibility:
- **JSON AST Representation:** Defined loyalty criteria rules using PostgreSQL JSONB schemas mapping operators, fields, values, and composite logical groups (AND, OR).
- **Recursive Criteria Evaluator:** Developed a tree-traversal logic (`TierEvaluationService`) capable of evaluating client context statistics (e.g., `totalSpent`, `totalOrders`, `isVip`) recursively against nested logic groups.
- **Integrations Ready:** Structured the rules parser to serve as a direct hook for batch nightly cron jobs to evaluate loyalty upgrades.

---

## 6. Testing & Quality Verification

### Automated Integration & Unit Tests
We verified the complete business lifecycle, REST contracts, and mappers using a comprehensive, robust test suite:
- **Tests Added:**
  - `MembershipPlanServiceTest` & `AdminMembershipPlanControllerTest` & `UserMembershipPlanControllerTest`
  - `UserMembershipServiceTest` & `AdminMembershipControllerTest` & `UserMembershipControllerTest`
  - `MembershipBenefitControllerTest`
  - `BenefitConfigurationControllerTest`
  - `TierCriteriaControllerTest`

- **Execution Results:**
  ```text
  [INFO] Results:
  [INFO] 
  [INFO] Tests run: 116, Failures: 0, Errors: 0, Skipped: 0
  [INFO] 
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  ```

### Checkstyle Conformity Audit
We enforced strict quality controls on formatting, package hierarchies, constructor injection, and imports:
- **Audit Results:**
  ```text
  [INFO] --- checkstyle:3.6.0:check (default-cli) @ loyalty-tier-system ---
  [INFO] Starting audit...
  Audit done.
  [INFO] You have 0 Checkstyle violations.
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  ```

---

## 7. Architecture & Design Decisions
1. **Context Lookup Security:** All self-service `/me` endpoints resolve user context dynamically from the security principal username (`mobileNumber`), preventing client-side identity forgery.
2. **Optimistic Locking:** Utilized `@Version` attributes across JPA mappings to gracefully handle concurrent updates, preventing overwrites on memberships.
3. **Lazy Fetch Loading:** Configured all entity associations using `FetchType.LAZY` to optimize query performance and completely eliminate N+1 database connection issues.
4. **MDC Correlation Logging:** Incoming requests cache transaction headers using a unique `X-Correlation-Id`, which is logged across downstream trace lines.
