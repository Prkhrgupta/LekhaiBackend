# core/domain

Core platform domain entities (admin, category, feature, role, shop, superadmin, users). Not shop-specific masters — those live in `core/account_master/` and `core/inventory_master/`.

## Public API

- `admin/AdminDetails`, `admin/UserInformation`
- `category/Categories`, `category/RolePermissions`
- `feature/Features`
- `roles/Roles`
- `shop/Shops`
- `superadmin/SuperAdminDetails`
- `users/Users`, `users/UserShopAccess`

## Dependencies

- `common/` (`ShopAwareEntity` where shop-scoped)

## How to Extend

- Add a core entity: create domain record here + matching repo in `core/repository/` (and DTO in `core/dto/` if no generated contract).

## Deep Dive

> Read `DETAILS.md` only when: modifying relationships between core entities or adding new domain tables.