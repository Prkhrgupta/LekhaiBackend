# core/dto

Hand-written DTOs for core flows — only where no generated contract exists. Organized by subdomain.

## Public API

- `admin/` — `AdminRegistrationRequest/Response`, `BaseUserEntity`
- `category/` — `CategoryCreationRequest/Response`, `CategoryResponse`, `EnableCategoryFeatureResponse`, `EnableCategoryWiseFeatures`
- `feature/` — `FeatureCreationResponse`, `FeatureResponse`, `ScreenFeatureCreationRequest`
- `shop/` — `BaseShopRequest`, `CreateShopExistingAdminRequest`, `CreateShopNewAdminRequest`, `ShopCreationResponse`
- `superadmin/` — `SuperAdminRegistrationRequest/Response`

## Dependencies

- none (plain records/POJOs)

## How to Extend

- Prefer the generated `in.lekhai.contract.model` types. Only hand-write a DTO here if no contract exists and one is needed internally.
- Use Java `record` for immutable value types.

## Deep Dive

> Read `DETAILS.md` only when: adding internal-only DTOs or mapping between contract and internal DTOs.