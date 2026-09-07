# core/inventory_master

Inventory master data: commodity, item category, item factory, stock item.

## Public API

**Controllers** (`controller/`): `CommodityController`, `ItemCategoryController`, `ItemFactoryController`, `StockItemController`

**Services** (`service/`): `CommodityService`, `ItemCategoryService`, `ItemFactoryService`, `StockItemService`

**Domains** (`domain/`): `Commodity`, `ItemCategory`, `ItemFactory`, `StockItem`

**Repositories** (`repository/`): `CommodityRepository`, `ItemCategoryRepository`, `ItemFactoryRepository`, `StockItemRepository`

## Dependencies

- `common/` (`ShopAwareEntity`, `Result`), `shop/`, `error/`

## How to Extend

Add an inventory master: follow the same 4-layer pattern as `core/account_master` (domain → repository → service → controller) + Flyway migration.

## Deep Dive

> Read `DETAILS.md` only when: changing stock item composition (references to other masters) or commodity-ledger linkage.