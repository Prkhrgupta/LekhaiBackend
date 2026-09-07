# core/service — details

## Files & Roles

| File | Role |
|---|---|
| `admin/AdminService.java` | Sign-up/activate admins, link to user accounts; coordinates with `authentication/UserService`. |
| `category/CategoryService.java` | Category CRUD, validation (via `error/controller/category/exception/*`), feature enablement. |
| `feature/FeatureService.java` | Feature/screen registry CRUD; enforces unique keys. |
| `feature/FeatureHierarchyBuilder.java` | Builds parent→child feature tree from flat `Features`. |
| `feature/FeatureMapService.java` | Cached/mapped feature lookups used by menu + permission checks. |
| `menu/MenuBuilder.java` | Assembles menu nodes from features + permissions for a user. |
| `menu/MenuService.java` | Menu endpoints backing `MenuController`. |
| `shop/ShopService.java` | Shop creation flows: new admin vs existing admin; seeds RLS setup. |
| `superadmin/SuperAdminService.java` | Provisioning of super-admins (idempotent). |

## Behavioral Notes

- Feature/menu logic depends on `RolePermissions` + `Features`; changes ripple into frontend menus via `MenuController`. Keep feature keys stable once shipped.
- Shop creation is a multi-step write — use `@ShopContextTransactional` (see `shop/`) so RLS scoping is correct during seeding.
- Caching is Caffeine-based (`common/config/CacheConfig`); invalidate feature/menu caches when registers change.