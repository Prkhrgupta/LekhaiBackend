# Definite menu ordering via `features.display_order`

Global, per-sibling order for the navigation **Menu** built from the **Feature** registry. Reuses the existing `features.display_order INTEGER` column; `getMenu` output order is the array position in `MenuResponse.mainMenu` (the `MenuItem` contract has no order field).

## Decisions

- Scope is per-sibling: roots ordered among roots (`parent_id IS NULL`), children among their siblings. No global sequence.
- Create is auto-append only: `POST /api/feature/create` assigns `COALESCE(MAX(display_order), -1) + 1` under that `parent_id` in the same transaction. No order field on `ScreenFeatureCreationRequest` (overrides the earlier Q1 idea of a client-supplied field).
- No reorder endpoint (deferred by explicit decision): reordering is done with direct SQL against `features.display_order` for now. A bulk-set `PUT /api/feature/reorder` remains a possible future addition, not implemented.
- Shop-owner reordering is deferred. `features` is global (no `shop_code`), so a shop-owner write would reorder every shop. Per-shop overrides (e.g. `shop_feature_order` or `favourites`) are a separate feature if needed.
- Read skips hidden: `MenuService`/`MenuBuilder` keep relative `display_order`, filtering by permission bits first. Residual `NULL`s sort last (already in `MenuBuilder`).
- No backfill migration: the 2 existing rows are fixed manually. Entity type aligns `Long` -> `Integer` to match the `INTEGER` column.
- `GET /api/feature/all` exposes `displayOrder` + `parentId` on `FeatureResponse`, sorted by `(parentId, displayOrder)`, so the admin reorder UI can render current order. All touched DTOs are internal (`core/dto`), no `lekhaiapispec` contract change.

## Reordering (manual SQL, no endpoint)

```sql
UPDATE features SET display_order = <position> WHERE id = <feature_id>;
```

Positions are per-sibling (`parent_id`, roots = `parent_id IS NULL`), written `0..n` in the desired order, one sibling set at a time.

## Notes

- `MenuBuilder` sorting is unchanged; `MenuItem`/`MenuResponse` contracts unchanged.
- No uniqueness constraint on `(parent_id, display_order)` — order is maintained by auto-append on create plus manual SQL for reorders.
