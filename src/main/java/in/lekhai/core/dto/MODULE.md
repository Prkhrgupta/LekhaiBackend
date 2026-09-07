# core/dto

Internal **form & payload types** for the platform's administrative flows — the data structures passed between the `core/controller` → `core/service` layers when there is no generated API contract to use. They carry the details of the actions platform staff perform: registering an admin, creating a shop, defining a category, enabling features, provisioning a super-admin.

## Business Goal

When the API spec defines a contract, the backend uses the generated `in.lekhai.contract.model` types — nothing here duplicates those. These hand-written DTOs exist only where an internal value object or request/response shape is needed that the contract doesn't cover. They keep the on-boarding flows (admin/shop/super-admin creation, category & feature enablement) self-contained and mapped.

## Public API

- `admin/` — `AdminRegistrationRequest/Response`, `BaseUserEntity` (shared admin fields: name/phone/email).
- `category/` — `CategoryCreationRequest/Response`, `CategoryResponse`, `EnableCategoryFeatureResponse`, `EnableCategoryWiseFeatures`.
- `feature/` — `FeatureCreationResponse`, `FeatureResponse`, `ScreenFeatureCreationRequest`.
- `shop/` — `BaseShopRequest`, `CreateShopExistingAdminRequest`, `CreateShopNewAdminRequest`, `ShopCreationResponse`.
- `superadmin/` — `SuperAdminRegistrationRequest/Response`.

## Dependencies

- none (plain records/POJOs).

## How to Extend

- Prefer the generated `in.lekhai.contract.model` types. Only hand-write a DTO here when no contract exists and one is needed internally.
- Use Java `record` for immutable value types; put validation annotations (`@NotNull`, `@Size`, ...) on request DTOs (responses stay plain).
- Extend/embed `BaseUserEntity` / `BaseShopRequest` rather than duplicating their fields.

## Deep Dive

> Read `DETAILS.md` only when: adding internal-only DTOs or mapping between contract and internal DTOs.