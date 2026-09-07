# core/repository

Spring Data JDBC repository interfaces for `core/domain` entities.

## Public API

- `admin/AdminDetailsRepo`, `admin/UserInformationRepo`
- `category/CategoriesRepo`, `category/RolePermissionsRepo`
- `feature/FeaturesRepo`
- `roles/RolesRepo`
- `shop/ShopsRepo`
- `superadmin/SuperAdminDetailsRepo`
- `users/UsersRepo`, `users/UserShopAccessRepo`
- `UserDetailsRepo` (root)

## Dependencies

- `core/domain/` (entities)

## How to Extend

- New core entity → add matching repo extending `CrudRepository`/`ListCrudRepository`, annotate `@Repository`. Raw SQL via `@Query` for custom lookups.

## Deep Dive

> Read `DETAILS.md` only when: writing custom `@Query` SQL or changing return types/projections.