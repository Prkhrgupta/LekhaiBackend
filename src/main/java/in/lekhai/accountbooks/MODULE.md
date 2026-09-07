# accountbooks

The firm's **ledger report** (khaata, खाता) — the accountant's view of every transaction in an account. In Tally terms this is the "Account Book / Ledger" report: pick an account and see its opening balance, all vouchers touching it, what was on the other side, and the running balance after every entry.

## Business Goal

This module turns posted vouchers back into something a shop owner or accountant can read and act on. For any **ledger** (a customer, supplier, bank, cash, or any other account), it produces a dated statement that shows:

- **Opening balance** brought forward (opening balance from the ledger master + net activity before the report start).
- **Every voucher** that touched the account in the chosen period, one line per voucher: date, voucher type (payment/receipt/contra/journal), the debit or credit amount, and — when a voucher involves several other parties — the **contra entries**, i.e. the other ledgers on the other side of the booking (e.g. for a payment, which bank/cash was credited).
- **Running balance** after each line, marked Dr/Cr, so the account's current position is visible at a glance.

The report defaults to the current **financial year** (1 Apr → 31 Mar, from the logged-in user's JWT claim) so accountants see the correct book year without re-entering dates. This is the same panel used for customer/supplier statements, bank reconciliation and audit.

## Public API

- `ledger/controller/LedgerReportController` — REST endpoints for the ledger report (implements the generated contract).
- `ledger/service/LedgerReportService` — builds the statement: loads the ledger, computes signed opening balance, paginated voucher lines with running balance, resolves sibling voucher entries and contra ledgers, and returns the contract response.

## Dependencies

Owns **no repositories** by design — it reads through interfaces owned by `core/account_master/` (ledger master + `LedgerSummaryProjection`) and `voucher/` (`VoucherEntryRepository`, `LedgerEntryWithBalanceProjection`). Also `common/` (financial-year helpers) and `error/` for not-found flows.

## How to Extend

To add a new report the firm's accountant would expect:

1. Add the endpoint in `LedgerReportController` and expose it via `lekhaiapispec`.
2. Implement the query in `LedgerReportService` **reusing** the existing projections/repos from `core/account_master/` and `voucher/` — keep this module free of its own data access.
3. Keep report queries time-bounded (financial-year aware) and verify any changed WHERE/ORDER is still covered by the report indexes (see Flyway `V17__add_ledger_report_indexes.sql`).

## Deep Dive

> Read `DETAILS.md` when: changing report queries, running-balance calculations, or adding a new report type.