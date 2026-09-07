# core/repository

The **data-access layer for the platform's people & structure** — how `core/service/` and `authentication/` read and write users, roles, shops, categories, features and permissions. This is pure infrastructure: no business rules live here, only persistence for the `core/domain` entities that model who can use the platform and what they may access.

## Business Goal

Every operation that on-boards a firm, manages an admin, or resolves a user's role and entitlements ends up here reading or writing one of these tables. Because row-level security is applied at the database-connection level (see `shop/`), repositories never hand-filter by shop code — isolation is already guaranteed by RLS, so the SQL stays simple and safe.

## Public API

- `UserDetailsRepo` — shared user+profile lookup.
- `admin/AdminDetailsRepo`, `admin/UserInformationRepo` — admin identity / user profile persistence.
- `category/CategoriesRepo`, `category/RolePermissionsRepo` — business categories and role→permission access.
- `feature/FeaturesRepo` — feature/screen registry queries.
- `roles/RolesRepo` — role lookup.
- `shop/ShopsRepo` — firm/tenant queries (by code or short name).
- `superadmin/SuperAdminDetailsRepo` — super-admin bootstrap records.
- `users/UsersRepo`, `users/UserShopAccessRepo` — users and their firm membership with roles.

## Dependencies

- `core/domain/` (the entities these interfaces persist).

## How to Extend

- New core entity → add a matching repo extending `CrudRepository`/`ListCrudRepository`, annotated `@Repository`.
- Custom lookups: raw PostgreSQL via `@Query`. Never hand-write `shop_code` filters — RLS handles isolation at the connection level.
- Return entities or `Optional` wrappers; don't introduce anonymous projections unless the query is trivial.

## Deep Dive

> Read `DETAILS.md` only when: writing custom `@Query` SQL or changing return types/projections.