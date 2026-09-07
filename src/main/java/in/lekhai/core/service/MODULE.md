# core/service

The **platform administration & on-boarding** layer — the business-side operations that run Lekhai as a SaaS, outside any individual firm's books. In Tally terms this is the equivalent of "create a new Company" and the setup that surrounds it: which businesses exist on the platform, who runs them, and what capabilities (features/menu) they are entitled to.

## Business Goal

Before a shop can post vouchers, the platform must provision it and decide what it can see. This module is where that happens:

- **On-board a new firm** (`shop/ShopService`) — create a shop on the platform with its own admin (either by registering a brand-new admin or linking an existing one). This is the "new company" step: the firm gets its RLS-scoped footing so every book it later creates is isolated. Shop creation is multi-step, so it runs inside the shop-scoped transaction machinery.
- **Manage administrators** (`admin/AdminService`) — sign up and activate the platform admins who operate shops, coordinated with the `authentication/` user accounts.
- **Run the platform's categories** (`category/CategoryService`) — the verticals/segments a business belongs to, including which *features* are enabled for shops in a category (entitlement control).
- **Feature catalogue** (`feature/`) — the registry of capabilities/screens a firm can use, organised as a hierarchy; keeping feature keys stable matters because the UI menu and permission checks are built from them.
- **Build the navigation menu** (`menu/`) — for a logged-in user, assemble the menu (like Tally's gateway/menu tree) from the features the role is permitted to see.
- **Provision super-admins** (`superadmin/SuperAdminService`) — the platform-level overseers, provisioned idempotently.

## Public API

- `admin/AdminService` — admin registration/activation.
- `category/CategoryService` — category CRUD + feature enablement.
- `feature/FeatureService`, `feature/FeatureHierarchyBuilder`, `feature/FeatureMapService` — the feature/screen registry and its tree building.
- `menu/MenuBuilder`, `menu/MenuService` — per-user/role menu assembly.
- `shop/ShopService` — the shop (firm) provisioning workflow.
- `superadmin/SuperAdminService` — super-admin provisioning.

## Dependencies

- `core/domain/`, `core/repository/`, `core/dto/`, `authentication/` (user provisioning), `common/` (caching, `Result`), `error/` (validation exceptions).

## How to Extend

To add a platform administration flow, create the service under the right subdomain folder (`admin/`, `category/`, `feature/`, `menu/`, `shop/`, `superadmin/`), use the `core` domain/repo/dto, and throw domain exceptions from `error/`. Invalidate feature/menu caches (Caffeine, in `common/`) when the registers change.

## Deep Dive

> Read `DETAILS.md` only when: changing feature/menu hierarchy algorithms or the provisioning flows.