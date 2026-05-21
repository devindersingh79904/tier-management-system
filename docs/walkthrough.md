# Walkthrough - JPA Entity Layer Implementation

We have successfully designed, implemented, and verified the JPA entity layer for the **Loyalty Tier System** backend. Below is a detailed walkthrough of the changes made, the files created, and the validation results.

---

## 1. Summary of Changes

### Configuration & Enums
- **Compiler Configuration (`pom.xml`):** Moved the MapStruct and Lombok annotation processors to the global configuration level of `maven-compiler-plugin`. This ensures annotation processing runs during both compilation and testing.
- **Removed Boilerplate:** Removed old `LoyaltyTier` boilerplate controllers, services, repositories, mappers, and enums to establish a clean state and prevent compilation issues from the primary key type change.
- **Created Enums (`com.devinder.loyalty.enums`):**
  - `UserRole`: `USER`, `ADMIN`, `SUPER_ADMIN`
  - `DurationUnit`: `DAY`, `MONTH`, `YEAR`
  - `CurrencyType`: `USD`, `INR`, `EUR`
  - `MembershipStatus`: `PENDING`, `ACTIVE`, `CANCELLED`, `EXPIRED`
  - `MembershipEventType`: `SUBSCRIBED`, `RENEWED`, `UPGRADED`, `DOWNGRADED`, `CANCELLED`, `EXPIRED`
  - `TransactionType`: `PAYMENT`, `RENEWAL`, `REFUND`, `UPGRADE_CHARGE`, `PARTIAL_REFUND`
  - `PaymentStatus`: `PENDING`, `SUCCESS`, `FAILED`, `REFUNDED`

---

## 2. JPA Entity Implementations (`com.devinder.loyalty.entity`)

Every entity inherits from `BaseEntity` and utilizes standard Lombok annotations (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`).

1. **`BaseEntity.java` (Modified):**
    - Refactored `id` from `Long` to `String` (storing standard UUID values generated via JPA 3.1 `GenerationType.UUID` and column length configured to 36).
   - Maintained `@Version` optimistic lock protection.
   - Maintained `@EntityListeners(AuditingEntityListener.class)` to automatically set and update `@CreatedDate` and `@LastModifiedDate`.

2. **`User.java` (New):**
   - Configured with a unique index on `mobile_number`.
   - Fields: `name`, `mobileNumber`, `passwordHash` (stored using BCrypt hashing; excluded from serialization via `@JsonIgnore`), `role` (enum), and `cohort`.

3. **`MembershipTier.java` (New):**
   - Designed for dynamic admin configurations using a `String` name.
   - Configured with a unique index on `name` and an index on `priority`.
   - Fields: `name`, `priority`, `description`, and `isActive`.

4. **`MembershipPlan.java` (New):**
   - Fields: `name`, `duration`, `durationUnit` (enum), `basePrice` (Long, representing paise/cents), `currency` (enum), and `isActive`.

5. **`UserMembership.java` (New):**
   - Configured with indexes on `user_id`, `status`, and `membership_tier_id`.
   - Relationships: `ManyToOne` with `User`, `MembershipPlan`, and `MembershipTier` using `FetchType.LAZY`.
   - Fields: `status` (enum), `startDate`, `endDate`, `purchasedPrice`, `discountAmount`, `finalPrice` (all price fields as `Long` cents/paise), and `autoRenew`.

6. **`MembershipBenefit.java` (New):**
   - Configured with a unique index on `name`.
   - Fields: `name`, `description`, and `isActive`.

7. **`BenefitConfiguration.java` (New):**
   - Relationships: `ManyToOne` with `MembershipBenefit`, `MembershipPlan` (nullable), and `MembershipTier` (nullable) using `FetchType.LAZY`.
   - Fields: `configurationJson` (mapped natively to PostgreSQL `jsonb` using Hibernate 6's `@JdbcTypeCode(SqlTypes.JSON)`) and `isActive`.

8. **`TierCriteria.java` (New):**
   - Relationships: `ManyToOne` with `MembershipTier` using `FetchType.LAZY`.
   - Fields: `criteriaJson` (mapped natively to PostgreSQL `jsonb` using `@JdbcTypeCode(SqlTypes.JSON)`) and `isActive`.

9. **`MembershipEvent.java` (New):**
   - Configured with an index on `created_at` for timeline auditing.
   - Relationships: `ManyToOne` with `UserMembership` using `FetchType.LAZY`.
   - Fields: `eventType` (enum), `oldValue`, `newValue`, and `reason`.

10. **`PaymentIntent.java` (New):**
    - Configured with a unique index on `idempotency_key` (retry-safe prevention of double charging) and an index on `transaction_id`.
    - Relationships: `ManyToOne` with `UserMembership` using `FetchType.LAZY`.
    - Fields: `transactionType` (enum), `amount` (Long, cents/paise), `paymentStatus` (enum), `transactionId`, `paymentProvider`, and `idempotencyKey`.

---

## 3. Documentation & Verification

- **Documentation Updates:**
  - Created [database-design.md](file:///Users/dsp/development/firstclub/loyalty-tier-system/docs/database-design.md) with database entity responsibilities, relationships, indexes, dynamic JSONB explanations, and concurrency strategies.
  - Updated [README.md](file:///Users/dsp/development/firstclub/loyalty-tier-system/README.md) to document the new entity structure, setup guide, execution steps, and environment variable configuration template.
- **Build Verification:**
  - Ran `./mvnw clean test` which successfully compiled all 9 entities, 7 enums, and configurations.
  - Successfully booted the Spring Context.
  - Validated physical schema generation in the test PostgreSQL database (log output shows creation of all 9 tables, indexes, constraints, and foreign key relations).

## 4. Code & Configuration Polish
- **Unused Import Removal:** Removed the unused `import org.springframework.validation.FieldError;` from `GlobalExceptionHandler.java`.
- **Deprecation Warning Resolution:** Updated the `CorrelationIdFilter.java` to use the non-deprecated `ContentCachingRequestWrapper(HttpServletRequest, int)` constructor with an explicit 1MB limit.
- **POM Cleanup:** Cleaned up empty XML tags (`<name/>`, `<description/>`, `<url/>`, `<licenses>`, `<developers>`, `<scm>`) from `pom.xml`.
- **Verification:** Ran `./mvnw clean test` to verify that there are no compilation warnings, no unused import warnings, and that the Spring Boot test context successfully initializes.
