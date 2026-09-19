# core/account_master — details

## Layered inventory (all 8 masters follow the same 4-layer pattern)

```
controller/<X>Controller   implements generated *Api, @PreAuthorize
service/<X>Service         business logic + validation
repository/<X>Repository   Spring Data JDBC (CrudRepository/ListCrudRepository)
domain/<X>                   entity, extends ShopAwareEntity unless system-level
```

Masters: **AccountGroup, Area, Broker, Ledger, State, Transport, PurchaseLedgerSetting, SaleLedgerSetting, TdsSection**. Plus `GstInDetails`, `Address` (owned by Ledger).

## Ledger specifics

- `domain/Ledger.java` — the master ledger of accounts for a shop (account group, GST in details, address, opening balance).
- `domain/LedgerSummaryProjection.java` — aggregate projection for summary views.
- `repository/LedgerRepository.java` — includes raw-SQL lookups used widely (voucher posting, reports). Changing signatures is a cross-module impact (see `voucher/`, `accountbooks/`).
- `utils/LedgerUtils.java` — name/code generation helpers.

## Goods & Service Tax / State

- `State` + `StatesSeeder` (from `resources/seeds/states.csv`) — system-level, NOT shop-scoped.
- `GstInDetails` — per-ledger GSTIN.

## Ledger Settings

- `PurchaseLedgerSetting` / `SaleLedgerSetting` (+ controllers/services/repos) — per-shop defaults used when vouchers post. Keep in sync with voucher validation rules.
- `utils/GstLedgerSettingRules` holds the GST rules both services apply before saving:
  - Supply kind: `IN_STATE` → CGST + SGST; `OUT_STATE`, `*_WITH_IGST`, `IMPORT`, purchase `SEZ` → IGST; `*_UNDER_LUT` → zero-rated.
  - `cgst/sgst/igst_percentage` are **derived** from `gst_rate` (never read from the request). Non-taxable and zero-rated rows store rate 0 and must not reference tax ledgers.
  - Intra-state rows need CGST + SGST ledgers and no IGST ledger; IGST rows the reverse. Cess % needs a cess ledger.
  - Reverse charge (purchase only): taxable, in-/out-state only, with RCM payable ledgers matching the split.
  - All referenced ledgers must exist; one active setting per (ledger, type) — duplicates are `409`.
- Validation failures are `LekhaiClientException` with an explicit `HttpStatus` (the handler NPEs on a null status).

## TDS

- `TdsSection` (`tds_section`, V22) — system-level like `State`, seeded by **Flyway INSERTs, not a CSV seeder**: statutory rates/thresholds change every Finance Act, so each change must be a new migration. Codes follow the Income-tax Act 1961 numbering; re-verify against the Income-tax Act 2025 (section 393) before relying on them.
- Party TDS details live on `ledger` (V23): `is_tds_applicable`, `tds_section_id`, `deductee_type`, lower deduction certificate (`ldc_*`). `LedgerService.validateTdsDetails` requires section + deductee type when applicable, and a complete certificate whose rate is below the section rate; `LedgerUtils.applyTdsDetails` clears every TDS field when TDS is not applicable.
- TDS is not yet deducted in vouchers — that belongs to purchase/journal/payment posting, using the party's section, the thresholds and the certificate.

## Seeding

- `AccountGroupSeeder` / `GroupSeeder` (`resources/seeds/account_groups.csv`) — boot-time idempotent master seeding.
- `StatesSeeder` — the ONLY place using direct `JdbcTemplate` (bulk insert).

## Behavioral Notes

- Ledger side and voucher posting must agree on account group / ledger references; `LekhaiClientException`s for "not found" flows come from `error/controller/account|purchaseledgersetting|saleledgersetting/exception/`.
- All shop-scoped masters rely on RLS (`shop/`) — never query without `ShopContext` active.