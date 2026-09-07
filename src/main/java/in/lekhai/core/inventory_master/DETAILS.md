# core/inventory_master — details

## Files & Roles

| File | Role |
|---|---|
| `Commodity.java` / `CommodityRepository.java` / `CommodityService.java` / `CommodityController.java` | Commodity master (product type). |
| `ItemCategory.java` ... | Item category master. |
| `ItemFactory.java` ... | Item factory/unit master. |
| `StockItem.java` ... | Stock item master — aggregates commodity + item category(+factory) into an enumerable stock-keeping item. |

## Behavioral Notes

- Mirrors Flyway migrations `V10__create_commodity_master_table.sql`, `V13__create_item_category_master_table.sql`, `V14__create_item_factory_master_table.sql`, `V15__create_stock_item_master_table.sql`.
- Note `V19__drop_commodity_ledger_columns.sql` — commodity↔ledger linkage was removed; do not reintroduce it. If ledger linkage is needed again, add a fresh migration.
- Stock-item creation validates existence of referenced masters and throws domain exceptions (`error/controller/.../exception/*`) on missing references.
- All SAME 4-layer pattern as `core/account_master` — reuse it for consistency.