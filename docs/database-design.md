# Database Design

This document details the database schema design, entity responsibilities, relationships, indexes, and scalability decisions for the Loyalty Tier System backend.

---

## 1. Entity-by-Entity Responsibility Explanation

The database consists of **9 tables** structured across Core Membership, Event Audit, and Billing domains.

| Table Name | Entity Class | Primary Purpose | Key Fields |
| :--- | :--- | :--- | :--- |
| `users` | `User` | Stores user credentials, contact information, and role/cohort grouping. | `mobileNumber` (unique login ID), `passwordHash` (ignored in serialization via `@JsonIgnore`), `role`, `cohort` |
| `membership_tiers` | `MembershipTier` | Defines system loyalty tiers (e.g. SILVER, GOLD, PLATINUM) with prioritization. | `name` (unique), `priority` (for rank sorting), `isActive` |
| `membership_plans` | `MembershipPlan` | Defines billing subscription plans (e.g., Monthly, Yearly) and base prices. | `name`, `duration`, `durationUnit`, `basePrice`, `currency`, `isActive` |
| `user_memberships` | `UserMembership` | Tracks active subscriptions, connecting a user to a plan and tier with active dates. | `user_id`, `membership_plan_id`, `membership_tier_id`, `status`, `startDate`, `endDate`, pricing fields |
| `membership_benefits` | `MembershipBenefit` | Catalog of privileges available (e.g., FREE_DELIVERY, PRIORITY_SUPPORT). | `name` (unique), `description`, `isActive` |
| `benefit_configurations`| `BenefitConfiguration`| Maps a benefit to a plan or a tier, storing parameters in a JSON format. | `membership_benefit_id`, plan/tier IDs (nullable), `configurationJson` |
| `tier_criteria` | `TierCriteria` | Defines dynamic rule structures (JSON) required to unlock a specific tier. | `membership_tier_id`, `criteriaJson` |
| `membership_events` | `MembershipEvent` | Append-only transaction log of all subscription adjustments for auditing. | `user_membership_id`, `eventType`, `oldValue`, `newValue`, `reason` |
| `payment_intents` | `PaymentIntent` | Tracks payment lifecycle and idempotency tokens for subscription charges. | `user_membership_id`, `transactionType`, `amount`, `paymentStatus`, `idempotencyKey` (unique) |

---

## 2. Entity Relationships & Fetch Types

- **Lazy Loading (FetchType.LAZY):** All `ManyToOne` relationships are explicitly configured with `fetch = FetchType.LAZY`. Loading associations eagerly defaults to generating complex, nested joins (and can lead to N+1 query loops). Lazy loading ensures database calls fetch only requested records.
- **Foreign Key Constraints:** Proper database foreign keys are created automatically by JPA to enforce referential integrity.
- **Cascade Strategy:** Cascade deletes (`CascadeType.REMOVE` or `orphanRemoval = true`) are completely avoided on core tables (`User`, `MembershipPlan`, `MembershipTier`, `UserMembership`). This ensures auditing data (e.g., events, payment records) is never deleted accidentally due to cascading effects.
- **JSON Serialization protection:** Standard DTO mappings (and using `@JsonIgnore` / `@JsonBackReference` on bidirectional fields where applicable) prevent endless serialization loops.

---

## 3. Separation of Plans and Tiers

A common anti-pattern is combining subscription billing plans and loyalty benefits into a single entity.
- **Why we separate them:** Billing (Plans) and Loyalty Perks (Tiers) represent distinct business domains. A **Plan** governs how much a user pays and how often (e.g., Monthly Plan at $10/month vs. Yearly Plan at $100/year). A **Tier** defines a user's loyalty status and privileges (e.g., Silver, Gold, Platinum status). 
- **Scalability Advantage:** Separating plans and tiers allows for flexible combinations: a user can purchase a *Monthly Plan* but hold a *Platinum Tier* (earned via spending or order frequency criteria). Conversely, an admin can change plan pricing without affecting tier privileges.

---

## 4. PostgreSQL JSONB Support

We map dynamic configurations (`criteriaJson` and `configurationJson`) using Hibernate 6's native `@JdbcTypeCode(SqlTypes.JSON)` mapped to PostgreSQL `jsonb` columns.
- **Criteria Rules Example:** A tier's criteria may combine order limits, cohort restrictions, or spend amounts (e.g., "min spend >= 5000 AND min orders >= 10").
- **Benefit Configuration Example:** A benefit like `EXTRA_DISCOUNT` requires a parameter (e.g., `{"discount_percent": 15}`), whereas `FREE_DELIVERY` might require a minimum purchase (e.g., `{"min_purchase_cents": 2000}`).
- **Why JSONB is Used:** Storing these structures in structured JSONB allows the rule engine to remain highly extensible without requiring frequent schema alterations or complex, slow entity-attribute-value (EAV) tables.

---

## 5. Indexing Strategy

To maintain sub-millisecond query performance as tables grow, we configure explicit indexes on frequent lookup fields:

1. **`users`:**
   - Unique Index on `mobile_number` (idx_users_mobile_number) - faster authentication and uniqueness checks.
2. **`membership_tiers`:**
   - Unique Index on `name` (idx_membership_tiers_name) - fast lookups.
   - Index on `priority` (idx_membership_tiers_priority) - speeds up ordering and qualification sorting.
3. **`user_memberships`:**
   - Index on `user_id` (idx_user_memberships_user_id) - speeds up profile/status dashboard queries.
   - Index on `status` (idx_user_memberships_status) - speeds up bulk billing/expiration batch cron tasks.
   - Index on `membership_tier_id` (idx_user_memberships_tier_id) - speeds up tier-wise analytics.
4. **`membership_events`:**
   - Index on `created_at` (idx_membership_events_created_at) - optimizes audit log loading and timeline lookups.
5. **`payment_intents`:**
   - Unique Index on `idempotency_key` (idx_payment_intents_idempotency_key) - enforces safety and prevents duplicate payment updates.
   - Index on `transaction_id` (idx_payment_intents_transaction_id) - optimizes customer support/webhook lookups.

---

## 6. Concurrency Control (Optimistic Locking)

Loyalty account operations are prone to race conditions (e.g., double spend operations or concurrent renewal requests).
- **How it works:** Each entity extends `BaseEntity` which defines a `@Version` attribute. When a transaction attempts to update a row, Hibernate checks if the version in the database matches the version read at the start of the transaction. If it doesn't match (meaning another thread modified the row in the meantime), an `ObjectOptimisticLockingFailureException` is thrown.
- **Use Cases protected:**
  - Duplicate subscription upgrades.
  - Race conditions during point accrual/benefit claims.
  - Concurrent renewal webhook actions from payment providers.

---

## 7. Payment Idempotency Reasoning

Payment processing is distributed and asynchronous, meaning networks can fail after a payment completes but before the backend records it.
- **Preventing Double Charging:** Every billing operation is initiated with a unique `idempotencyKey`. The unique database index on `idempotency_key` prevents the system from recording or processing the same checkout intent twice, even under heavy network retries.
