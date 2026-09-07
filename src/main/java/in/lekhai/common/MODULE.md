# common

The **shared foundation** of the accounting system — the plumbing every module leans on so that the whole product behaves consistently: one response shape for the UI, one audited base record for every firm's data, and one way to export and cache. There's no business flow here, but every business flow uses it.

## Business Goal

Consistency and data-integrity rules that must hold across the entire product live here:

- **One API envelope for everything** — `Result<T>` is how every endpoint answers a client, success or error. The UI can always parse the same shape, and errors are never assembled ad hoc in controllers.
- **Every firm's record is audited and isolated** — `ShopAwareEntity` is the base for all shop-scoped masters and transactions (ledgers, vouchers, stock items...). It carries the shop code (RLS tenant) plus `createdAt`/`updatedAt`, and a callback auto-injects the shop code on save. New business data inherits tenant isolation and an audit trail for free.
- **Excel export** — a generic exporter driven by column definitions is used wherever the firm needs an Excel report from the backend (ledger exports, EWB summaries).
- **Fast, frequent lookups** — Caffeine caches for stable reference data (states, account groups) keep day-to-day screens snappy.
- **Shared domain vocabulary** — `AccountEntryType` (debit/credit), `JwtConstants` (token claims), `SuperAdminConstants`, and the **financial-year** date helpers (1 Apr–31 Mar) that keep reports aligned with the Indian accounting year.

## Public API

- `Result<T>` — the standard success/error envelope; use the factories, don't hand-build responses.
- `domain/ShopAwareEntity` — abstract base for all shop-scoped entities (shop code + audit timestamps); `setShopCodeIfNull(...)` is how the shop code is stamped.
- `ShopAwareEntityCallback` — save-time hook that stamps the shop code from `ShopContext` (see `shop/`).
- `excel/ExcelExporter`, `excel/ExcelColumn`, `excel/WorkbookCustomizer` — POI-based export helpers.
- `config/CacheConfig` — Caffeine cache manager.
- `AccountEntryType`, `JwtConstants`, `SuperAdminConstants` — shared constants.
- `util/FinancialYearDateUtil` — financial-year date helpers.

## Dependencies

- `shop/` (ShopContext, read by the save-time callback), `error/` (invalid-shop exception surfaces on save), org.springframework.data, Apache POI, Caffeine, Jackson.

## How to Extend

- **New shop-scoped record:** extend `ShopAwareEntity`; never set `shop_code` manually — rely on `setShopCodeIfNull`.
- **New endpoint:** return through `Result<T>`; do not create another envelope.
- **New export:** define `ExcelColumn`s and reuse `ExcelExporter`.
- Keep assertions/constants (Dr/Cr, JWT names, financial-year) here so they don't proliferate per-module.

## Deep Dive

> Read `DETAILS.md` only when: changing audit/shop-code injection behaviour, extending the exporter, or modifying `Result<T>`.