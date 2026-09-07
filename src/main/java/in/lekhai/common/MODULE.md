# common

Shared primitives used across the backend: base entity, response envelope, Excel export, constants, caching.

## Public API

- `Result<T>` — standard success/error envelope returned to clients (record).
- `domain/ShopAwareEntity` — abstract base for all shop-scoped entities (`shopCode` + `createdAt`/`updatedAt` auditing).
- `ShopAwareEntityCallback` — Spring Data JDBC `BeforeConvertCallback`; auto-injects `shop_code` from `ShopContext` (see `shop/`).
- `excel/ExcelExporter`, `ExcelColumn`, `WorkbookCustomizer` — Apache POI export helpers.
- `config/CacheConfig` — Caffeine cache configuration.
- `AccountEntryType`, `JwtConstants`, `SuperAdminConstants` — shared constants.
- `util/FinancialYearDateUtil` — financial year date helpers.

## Dependencies

- `shop/` (ShopContext)
- `error/` (exceptions on invalid input)
- org.springframework.data., Apache POI, Caffeine, Jackson

## How to Extend

- New shop-scoped entity: extend `ShopAwareEntity`; do NOT set `shop_code` manually — use `setShopCodeIfNull`.
- New endpoints: reuse `Result<T>`; do not create another envelope.
- New export: add `ExcelColumn`s and reuse `ExcelExporter`.

## Deep Dive

> Read `DETAILS.md` only when: changing audit/shop-code injection behavior, extending the exporter, or modifying `Result<T>`.