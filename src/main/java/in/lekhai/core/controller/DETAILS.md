# core/controller — details

## Files & Roles

| File | Role |
|---|---|
| `admin/AdminController.java` | Admin CRUD + activation; implements `AdminApi`-style contract. |
| `category/CategoryController.java` | Category CRUD + category-feature enablement. |
| `feature/FeatureController.java` | Feature/screen registry + hierarchy endpoints. |
| `menu/MenuController.java` | Builds/burns menu tree for the authenticated user. |
| `shop/ShopController.java` | Shop provisioning (new/existing admin). |
| `superadmin/SuperAdminController.java` | Super-admin provisioning endpoints. |

## Behavioral Notes

- Controllers are thin: `@Valid` on body, `@PreAuthorize` on methods, one service call, wrap in `Result<T>` return shape (via `Result.success(...)` or controller-returning handler).
- `SecurityExpressions` (in `authentication/utils/`) supplies role/ownership SPEL. Never inline raw role strings in annotations.
- Cross-cutting concern: these endpoints touch both `authentication` (provisioning) and `core/service`. When in doubt about authorization, compare with `admin`/`superadmin` endpoints before copying patterns.