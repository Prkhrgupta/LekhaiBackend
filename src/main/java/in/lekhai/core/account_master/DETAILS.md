# core/account_master — details

## Layered inventory (all 8 masters follow the same 4-layer pattern)

```
controller/<X>Controller   implements generated *Api, @PreAuthorize
service/<X>Service         business logic + validation
repository/<X>Repository   Spring Data JDBC (CrudRepository/ListCrudRepository)
domain/<X>                   entity, extends ShopAwareEntity unless system-level
```

Masters: **AccountGroup, Area, Broker, Ledger, State, Transport, PurchaseLedgerSetting, SaleLedgerSetting**. Plus `GstInDetails`, `Address` (owned by Ledger).

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

## Seeding

- `AccountGroupSeeder` / `GroupSeeder` (`resources/seeds/account_groups.csv`) — boot-time idempotent master seeding.
- `StatesSeeder` — the ONLY place using direct `JdbcTemplate` (bulk insert).

## Behavioral Notes

- Ledger side and voucher posting must agree on account group / ledger references; `LekhaiClientException`s for "not found" flows come from `error/controller/account|purchaseledgersetting|saleledgersetting/exception/`.
- All shop-scoped masters rely on RLS (`shop/`) — never query without `ShopContext` active.