# JWT Strategy

This document details the JWT (JSON Web Token) strategy, claims structure, verification mechanism, and expiration policies implemented in the Loyalty Tier System.

---

## 1. Token Lifetimes & Expiries

Token expiration policies are externalized through environment variables configured in the `.env` file:

| Token Type | Variable Name | Default Value (Demo) | Production Recommended Value |
| :--- | :--- | :--- | :--- |
| **Access Token** | `ACCESS_TOKEN_EXPIRY_MINUTES` | `300` minutes (5 hours) | `15` minutes |
| **Refresh Token** | `REFRESH_TOKEN_EXPIRY_DAYS` | `30` days | `7` to `14` days |

> [!WARNING]
> **Production Notice:**
> The default access token lifetime is set to 300 minutes to ensure smooth, uninterrupted user sessions during system testing and evaluator reviews. In a production environment, access tokens must have a much shorter lifespan (e.g., 15 minutes) to minimize the window of vulnerability for compromised tokens.

---

## 2. Signature and Cryptographic Key

*   **Signing Algorithm:** HMAC using SHA-256 (`HS256`).
*   **Key Source:** Derived dynamically from the `JWT_SECRET` environment variable.
*   **Key Length:** The secret must be at least 256 bits (32 bytes) long to fulfill HS256 requirements. The default hex key provided in the template is 64 bytes (512 bits) to ensure high-entropy strength.

---

## 3. JWT Claims Structure

### Access Token Claims

The Access Token is stateless and contains claims that enable authorization checks without hitting the database on every HTTP request.

```json
{
  "sub": "9876543210",              // Subject (User's Mobile Number)
  "userId": "d7a1e5f8-c6b2-4f3e-...",// Custom Claim: User Primary Key ID
  "role": "USER",                   // Custom Claim: Authorization Role
  "iat": 1716300000,                // Issued At (Epoch timestamp)
  "exp": 1716318000                 // Expiration Time (Epoch timestamp)
}
```

### Refresh Token Claims

The Refresh Token is lightweight and only contains the subject claim. It is signed cryptographically to prove session authenticity.

```json
{
  "sub": "9876543210",              // Subject (User's Mobile Number)
  "iat": 1716300000,                // Issued At
  "exp": 1718892000                 // Expiration Time (30 days later)
}
```

---

## 4. Token Validation Flow

1.  **Extract Header:** The client passes the Access Token in the `Authorization` header as `Bearer <token>`.
2.  **Verify Signature:** The server verifies the signature using the configured `JWT_SECRET`.
3.  **Check Expiry:** The server verifies that the current system time is before the `exp` claim.
4.  **Extract Authorities:** The server extracts the `role` claim and prefixes it with `ROLE_` to build the Spring Security authentication context.
5.  **Exception Handling:** If the token signature is invalid, tampered with, or expired, the validation fails, setting a request attribute which `CustomAuthenticationEntryPoint` checks to output structured errors (`401 Unauthorized` containing `ERR_UNAUTHORIZED`).
