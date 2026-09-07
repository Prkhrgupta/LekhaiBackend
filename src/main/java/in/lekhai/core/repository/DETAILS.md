# core/repository — details

## Files & Roles

| File | Role |
|---|---|
| `UserDetailsRepo.java` | Shared user detail lookup (joins user + info). |
| `admin/AdminDetailsRepo.java`, `admin/UserInformationRepo.java` | Admin/profile persistence. |
| `category/CategoriesRepo.java`, `category/RolePermissionsRepo.java` | Category + role-permission access. |
| `feature/FeaturesRepo.java` | Feature/screen registry queries. |
| `roles/RolesRepo.java` | Role lookup. |
| `shop/ShopsRepo.java` | Shop/tenant queries (by code/short name). |
| `superadmin/SuperAdminDetailsRepo.java` | Super-admin bootstrap. |
| `users/UsersRepo.java`, `users/UserShopAccessRepo.java` | Users + user-shop-role joins. |

## Behavioral Notes

- Spring Data JDBC: no JPA/EntityManager. Repos extend `CrudRepository`/`ListCrudRepository`.
- Custom lookups use `@Query` with raw PostgreSQL; keep SQL consistent with row-level security (no `shop_code` filtering needed manually since RLS applies at connection level).
- Handlers return entities or optional wrappers; do not return `Map`/anonymous projections unless trivial.