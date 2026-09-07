# core/domain — details

## Files & Roles

| File | Role |
|---|---|
| `admin/AdminDetails.java` | Farida/owner admin details linked to a user. |
| `admin/UserInformation.java` | User profile/info beyond credentials. |
| `category/Categories.java` | Category entity (hierarchical, per organization). |
| `category/RolePermissions.java` | Permissions bound to roles (feature access). |
| `feature/Features.java` | Feature/screen registry rows. |
| `roles/Roles.java` | Role definitions (SaaS roles: super admin, admin, owner, staff). |
| `shop/Shops.java` | Shop/tenant entity; source of `shopCode`. |
| `superadmin/SuperAdminDetails.java` | Super-admin bootstrap identity. |
| `users/Users.java` | Application users, links to shop access. |
| `users/UserShopAccess.java` | Join: user ↔ shop with role. |

## Behavioral Notes

- Mirrors Flyway migrations `V3` (user mgmt) and `V5` (access control). Schema additions = new migration + new/modified entities here.
- `Shops`/`Users`/`UserShopAccess` are referenced by `authentication/` provisioning flows.
- Feature/menu rendering uses `Features` + `RolePermissions`; changing shape affects `core/service/feature/*` and `core/service/menu/*`.