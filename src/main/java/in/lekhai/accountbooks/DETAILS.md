# accountbooks — details

## Files & Roles

| File | Role |
|---|---|
| `ledger/controller/LedgerReportController.java` | Report endpoints (e.g., purchase/sales ledger export). |
| `ledger/service/LedgerReportService.java` | Builds filtered/ordered ledger result sets from raw SQL through Spring Data JDBC projections. |

## Behavioral Notes

- This module has NO own repositories — it reads via repository interfaces/projections owned by `core/account_master/` and `voucher/`. Keep it that way to avoid duplicate data access.
- Report queries should be time-bounded (financial-year aware via `common/util/FinancialYearDateUtil` or `core/.../utils/DateUtils`).
- Frequently run reports map to Flyway index migrations (see `V17__add_ledger_report_indexes.sql`). If you change a report's WHERE/ORDER, verify indexes still cover it.