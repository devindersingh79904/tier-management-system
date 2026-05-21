# Role Access Matrix

This document defines the Role-Based Access Control (RBAC) matrix for the Loyalty Tier System API endpoints.

---

## 1. Roles Definition

The system recognizes three roles:

*   **`USER`:** Authenticated customer accounts. Exposes customer self-service actions.
*   **`ADMIN`:** System administrators. Allowed to perform create, read, update, and soft delete operations on tiers, plans, and benefits.
*   **`SUPER_ADMIN`:** High-privileged administrator role sharing admin functionality.

---

## 2. API Role Permissions Matrix

| HTTP Method | Endpoint | Required Role / Authority | Description |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/v1/auth/signup` | Public / Unauthenticated | Registers a new user account |
| **POST** | `/api/v1/auth/login` | Public / Unauthenticated | Authenticates user credentials and issues JWTs |
| **GET** | `/api/v1/me/profile` | `isAuthenticated()` | Retrieves the logged-in user profile |
| **GET** | `/api/v1/me/memberships` | `isAuthenticated()` | Retrieves memberships for the logged-in user context |
| **GET** | `/api/v1/tiers` | `isAuthenticated()` | List membership tiers (paginated) |
| **GET** | `/api/v1/tiers/{id}` | `isAuthenticated()` | Retrieve a membership tier by ID |
| **POST** | `/api/v1/admin/tiers` | `hasRole('ADMIN')` or `hasRole('SUPER_ADMIN')` | Create a new loyalty tier |
| **PUT** | `/api/v1/admin/tiers/{id}` | `hasRole('ADMIN')` or `hasRole('SUPER_ADMIN')` | Update details of a loyalty tier |
| **DELETE** | `/api/v1/admin/tiers/{id}` | `hasRole('ADMIN')` or `hasRole('SUPER_ADMIN')` | Soft delete/deactivate a loyalty tier |
| **POST** | `/api/v1/admin/plans` | `hasRole('ADMIN')` or `hasRole('SUPER_ADMIN')` | Create a new billing plan |
| **GET** | `/api/v1/admin/plans` | `hasRole('ADMIN')` or `hasRole('SUPER_ADMIN')` | List all billing plans |
| **GET** | `/api/v1/admin/memberships` | `hasRole('ADMIN')` or `hasRole('SUPER_ADMIN')` | List all customer membership records |

---

## 3. Route Security Mapping Strategy

Endpoints are protected at the Spring Security config level for public/authenticated routes and at the Controller method level using `@PreAuthorize`:

*   **Public Access:** Explicitly permitted in `SecurityConfig.java` (e.g. `/api/v1/auth/**`, swagger, `/`).
*   **Method Access:** Handled via `@PreAuthorize("isAuthenticated()")` for user controllers and `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` for admin controllers.
