# core/domain

The **platform's people and structure** — the entities that describe who can use Lekhai, which firms they belong to, and what each firm is entitled to. In accounting-product terms this is the "users, roles & permissions" layer that decides who sits where in the firm and which screens they may open. Shops' actual accounting masters (ledgers, inventory) live elsewhere, in `core/account_master/` and `core/inventory_master/`.

## Business Goal

Before any ledger or voucher exists, the platform needs to know *who* the actors are and *which business* they operate. This module models that:

- **Users & shop access** — an application user (`Users`), their profile (`UserInformation`), and the join that says which firm(s) they work for and with which role (`UserShopAccess`).
- **Shops** — the firm/tenant entity itself; the source of the `shopCode` that the whole row-level security model revolves around.
- **Roles** — the SaaS role definitions (super admin, admin, owner, staff) that drive the permission system.
- **Admins & super-admins** — the owner/admin bootstrap identities behind shop ownership (`AdminDetails`, `SuperAdminDetails`).
- **Categories & entitlements** — business categories (`Categories`, hierarchical) and the **Features** registry with `RolePermissions`, which together decide what screens/capabilities a role may use for a firm.

`authentication/` consumes this data when provisioning logins, and `core/service/` builds menus and enforces entitlements from the feature/permission rows.

## Public API

- `admin/AdminDetails`, `admin/UserInformation` — owner/admin identity + user profile.
- `category/Categories`, `category/RolePermissions` — business categories and role→feature permissions.
- `feature/Features` — the feature/screen registry.
- `roles/Roles` — SaaS role definitions.
- `shop/Shops` — the firm/tenant; source of `shopCode`.
- `superadmin/SuperAdminDetails` — platform super-admin identity.
- `users/Users`, `users/UserShopAccess` — users and their shop membership with role.

## Dependencies

- `common/` (`ShopAwareEntity` where a record is shop-scoped).

## How to Extend

- New platform entity: add the domain record here, the matching repo in `core/repository/`, and a DTO in `core/dto/` only if no generated contract exists. Schema changes mean a new append-only Flyway migration, never editing an applied one.

## Deep Dive

> Read `DETAILS.md` only when: modifying relationships between core entities or adding new domain tables.