# core/controller

Top-level platform controllers: admin, category (platform-level), feature, menu, shop, superadmin.

## Public API

- `admin/AdminController` — admin management endpoints.
- `category/CategoryController` — category CRUD.
- `feature/FeatureController` — feature/screen registry CRUD.
- `menu/MenuController` — menu tree endpoints.
- `shop/ShopController` — shop create/manage.
- `superadmin/SuperAdminController` — super-admin provisioning endpoints.

## Dependencies

- `core/service/` (delegation), `authentication/` (`SecurityExpressions` for `@PreAuthorize`), `common/` (`Result`)

## How to Extend

- New endpoint here: implement the generated `*Api` interface from `lekhaiapispec`, delegate to `core/service/`, annotate `@PreAuthorize(SecurityExpressions.X)`. No business logic in controllers.

## Deep Dive

> Read `DETAILS.md` only when: changing authorization wiring, controller-level validation, or endpoint contracts.