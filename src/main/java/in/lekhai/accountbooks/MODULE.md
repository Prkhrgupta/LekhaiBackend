# accountbooks

Dedicated ledger report slice: purchase/sales ledger report endpoints.

## Public API

- `ledger/controller/LedgerReportController` — ledger report REST endpoints.
- `ledger/service/LedgerReportService` — query/aggregation for ledger reports (uses projections from other modules).

## Dependencies

- `core/account_master/` (ledger domain + `LedgerSummaryProjection`), `voucher/` (entries + `LedgerEntryWithBalanceProjection`), `error/`, `common/`

## How to Extend

- Add a report: add endpoint in `LedgerReportController`, implement query in `LedgerReportService`, reuse existing projections.

## Deep Dive

> Read `DETAILS.md` only when: changing report queries, balances, or adding new report types.