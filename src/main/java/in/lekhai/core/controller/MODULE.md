# core/controller

The **REST surface of platform administration** — the endpoints that platform admins and super-admins call to on-board firms, configure entitlements, and manage the menu. This is the HTTP counterpart of `core/service/`: thin, authorised entry points with **no business logic**.

## Business Goal

The platform's owner-facing screens (console/back-office) talk to Lekhai through these endpoints. They expose the same administration actions the services already described, over HTTP:

- **Admin management** — create/activate the platform admins who run shops.
- **Category management** — create business categories and decide which features a category's shops are entitled to.
- **Feature registry** — maintain the catalogue of feature/screen capabilities and their hierarchy.
- **Menu** — build the navigation menu for the currently logged-in user.
- **Shop provisioning** — on-board a new firm (new or existing admin).
- **Super-admin provisioning** — the platform's highest-level accounts.

Every endpoint validates its input (`@Valid`), enforces who may call it (`@PreAuthorize` using role/shop-expressions), delegates one service call, and returns the standard `Result<T>` envelope. No endpoint here writes business rules.

## Public API

- `admin/AdminController` — admin management endpoints.
- `category/CategoryController` — category CRUD + category-feature enablement.
- `feature/FeatureController` — feature/screen registry + hierarchy endpoints.
- `menu/MenuController` — per-user menu tree endpoints.
- `shop/ShopController` — shop create/manage.
- `superadmin/SuperAdminController` — super-admin provisioning endpoints.

## Dependencies

- `core/service/` (delegation), `authentication/` (`SecurityExpressions` for `@PreAuthorize`, never inline raw role strings), `common/` (`Result`).

## How to Extend

- New platform endpoint: implement the generated `*Api` interface from `lekhaiapispec`, delegate to `core/service/`, annotate `@PreAuthorize(SecurityExpressions.X)`, and return the standard envelop shape. Keep controllers thin.
- When unsure about authorisation (admin vs super-admin), mirror the pattern of the existing `admin`/`superadmin` endpoints before inventing a new one.

## Deep Dive

> Read `DETAILS.md` only when: changing authorization wiring, controller-level validation, or endpoint contracts.