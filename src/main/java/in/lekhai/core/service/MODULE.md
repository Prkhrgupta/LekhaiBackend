# core/service

Core platform services: admin, category, feature (hierarchy), menu, shop, superadmin.

## Public API

- `admin/AdminService` — admin CRUD logic.
- `category/CategoryService` — category CRUD + feature enablement.
- `feature/FeatureService` — feature registry CRUD.
- `feature/FeatureHierarchyBuilder`, `feature/FeatureMapService` — feature tree building.
- `menu/MenuBuilder`, `menu/MenuService` — menu tree building per user/role.
- `shop/ShopService` — shop provisioning workflow.
- `superadmin/SuperAdminService` — super-admin provisioning workflow.

## Dependencies

- `core/domain/`, `core/repository/`, `core/dto/`, `core/util/`, `authentication/` (provisioning), `common/`, `error/`

## How to Extend

- Add a core flow: service in the right subdomain folder, use repos + domain + dto; throw domain exceptions from `error/`.

## Deep Dive

> Read `DETAILS.md` only when: changing feature/menu hierarchy algorithms or provisioning flows.