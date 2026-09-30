# core/inventory_master — details

## Files & Roles

| File | Role |
|---|---|
| `Commodity.java` / `CommodityRepository.java` / `CommodityService.java` / `CommodityController.java` | Commodity master (product type). |
| `ItemCategory.java` ... | Item category master. |
| `ItemFactory.java` ... | Item factory/unit master. |
| `StockItem.java` ... | Stock item master — aggregates commodity + item category(+factory) into an enumerable stock-keeping item. Points at one Primary UOM (required) and optionally one Alternate UOM with a fixed positive conversion factor; factor must be absent when no Alternate is set and the Alternate must differ from the Primary. |
| `Uom.java` / `UomRepository.java` / `UomService.java` / `UomController.java` | UOM master — shop-owned unit labels mapped to the contract-owned GST quantity-code enum. |
| `StockItemUomValidator.java` | UOM reference rules for stock items (Primary required and active, Alternate distinct with positive factor, factor absent without Alternate). |

## Behavioral Notes

- Mirrors Flyway migration `V10__create_commodity_master_table.sql`, `V12__create_item_category_master_table.sql`, `V13__create_item_factory_master_table.sql`, `V14__create_stock_item_master_table.sql` (which also creates `uom_master`; `V14` was edited in place under the clean-run waiver since the Stock Item table is not yet released — do not apply the append-only rule to that edit).
- Stock-item creation validates existence of referenced masters and throws domain exceptions (`error/controller/.../exception/*`) on missing references.
- A UOM referenced by any active stock item (as Primary or Alternate) cannot be soft-deleted (`UomInUseException`); the quantity code of such a unit is frozen while renaming stays allowed.
- Quantity-code validation lives at contract and application level only, never as a database check; the factor column is nullable with a `> 0` check constraint.
- All SAME 4-layer pattern as `core/account_master` — reuse it for consistency.