# Concurrency & Lock Management

This document details how the Loyalty Tier System maintains data integrity and handles concurrent update operations safely without sacrificing database performance.

---

## 1. Concurrency Challenges in Subscription Systems

In multi-tenant, high-throughput subscription or loyalty platforms, data concurrency anomalies are common. 

### Core Concurrency Scenarios
1.  **Double Point Submissions:** A customer performs two actions at the same time. Both threads read a balance of `100` points. Thread A adds `50` points and saves `150`. Thread B adds `30` points and saves `130`. The last save wins, resulting in a lost update (the balance should be `180`).
2.  **Concurrent Tier Upgrades:** A user clicks "Upgrade Tier" twice in rapid succession. Two processes attempt to read the current tier, validate criteria, and insert upgrade logs. Without protection, this can result in duplicate tier history logs or incorrect point calculations.

---

## 2. Optimistic Locking Strategy (Zero-Blocking Concurrency)

To solve the lost-update problem, we use **Optimistic Locking** instead of Pessimistic Locking.

### Why Optimistic Locking?
*   **Pessimistic Locking (`SELECT ... FOR UPDATE`):** Locks rows in the database, blocking other transactions. This hurts scalability, limits database throughput, and can cause deadlocks.
*   **Optimistic Locking:** Assumes conflicts are rare. It allows concurrent reads but checks for conflicts at the moment of update. This provides high read performance while guaranteeing write integrity.

### Implementation with `@Version`
Every entity inherits the `version` column from the shared `BaseEntity`:

```java
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
```

*   When Hibernate reads a row, it retrieves the current `version` value (e.g. `1`).
*   When executing the update query, Hibernate appends a version check to the SQL query:
    ```sql
    UPDATE loyalty_tiers 
    SET minimum_points = 1500, version = 2, updated_at = NOW() 
    WHERE id = 1 AND version = 1;
    ```
*   If another transaction updated the row in the meantime, the version in the database would be `2`. The `UPDATE` query affects `0` rows.
*   Hibernate detects that no rows were updated and throws an `ObjectOptimisticLockingFailureException`.

---

## 3. Handling Optimistic Lock Exceptions

When an `ObjectOptimisticLockingFailureException` is thrown, the transaction is immediately rolled back. 
The exception is caught by the `GlobalExceptionHandler`, which returns a clean `409 Conflict` status with an informative error message:

```java
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        log.error("Concurrent update conflict (optimistic lock failure): {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
                "The resource was updated by another process. Used optimistic locking to avoid concurrent subscription update conflicts.",
                ErrorConstants.CONFLICT,
                HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
```

---

## 4. Preventing Duplicate Subscriptions (Database-Level Integrity)

While optimistic locking protects existing rows, it does not prevent duplicate record insertions (e.g. creating two loyalty tiers with the same name simultaneously).

To prevent duplicates:
*   **Unique Database Constraints:** Enforce constraints directly on database tables (e.g., `@Column(unique = true)`).
    ```java
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    ```
*   **Constraint Violation Handling:** If two threads attempt to insert the same unique value concurrently, the database throws a constraint violation exception. The application handles this as a validation/conflict error, aborting the second transaction.

---

## 5. Transaction Boundaries & Concurrency Guidelines

To minimize concurrency conflicts, follow these guidelines when writing transactional code:

*   **Keep Transactions Short:** Do not perform external network calls (e.g., calling a payment gateway or sending an email) inside a `@Transactional` block. This keeps database connections open longer, increasing the likelihood of optimistic locking conflicts.
*   **Execute Business Logic First:** Perform API lookups and calculations before starting the transaction, using the transactional database phase only for persistence and integrity verification.
*   **Read-Only Operations:** Annotate search and retrieval operations with `@Transactional(readOnly = true)` to avoid version checks and reduce lock overhead in the database engine.
