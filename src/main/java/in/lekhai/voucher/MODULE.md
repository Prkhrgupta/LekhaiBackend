# voucher

Recording and posting **every financial transaction of the firm** — the heart of the double-entry accounting engine. In Tally terms, these are the Voucher Entry screens (F4 Contra, F5 Payment, F6 Receipt, F7 Journal): the source documents from which all ledgers and reports are built.

## Business Goal

All money that moves into, out of, or between a shop's accounts is captured here as a voucher, then posted to the ledger in a way that keeps the books balanced. The voucher types map directly to real bookkeeping acts:

- **Payment** — money the firm pays out (to a supplier, expense, loan repayment).
- **Receipt** — money the firm receives (from a customer, advance, sale proceeds).
- **Contra** — a transfer between the firm's own cash & bank accounts (e.g. "cash deposited in bank"); no external party involved.
- **Journal** — adjustments and non-cash entries (opening balances, provisions, corrections), typically used by the accountant.

Each voucher is validated first, then converted into a set of double-entry lines (ledger + debit + credit + narration) and posted so that **total debits always equal total credits** — the invariant that keeps Lekhai's books accurate, exactly as Tally would enforce. Every posted voucher also receives a bill number: either a sequential per-type counter (e.g. `PY-7`, `RC-3`) or, when the request carries a `series_code`, the next running number of that custom series in the voucher's financial year (displayed as `prefix+number+suffix`, e.g. `CASH/43`).

Posting mutates the ledgers' balances, so it happens atomically inside a shop-scoped transaction (RLS active) — a wrong step here corrupts the books, not just a single record.

## Public API

- `controller/VoucherController` — API surface to create and list vouchers plus custom-series CRUD (implements the generated contract).
- `service/VoucherIntakeService` — the single intake for all voucher kinds: **validate → build posting request → post → respond**. Direction (payment vs receipt) and Account-group policy (cash-or-bank vs any ledger) are data inside this module, not separate classes: `processPayment`, `processReceipt`, `processContra`, `processJournal`. Each also forwards the optional `series_code` + `bill_no` into the posting request.
- `service/posting/VoucherPostingService` — the posting engine: builds the voucher + its entry rows, enforces the debits==credits rule, numbers via the legacy counter or the custom series, and persists under a shop-scoped transaction.
- `service/VoucherCounterService` — legacy sequential voucher numbers per type via one atomic `reserveNextNumber` call (used only when the request carries no `series_code`).
- `service/VoucherSeriesService` — `custom_voucher_series` CRUD for SALES, PAYMENT, RECEIPT, CONTRA, JOURNAL: create/list/update with FY validation, 16-char bill check, post-first-use immutability.
- `service/SeriesReserveService` — active FY-scoped bill allocator behind the numbering seam: `FOR UPDATE` lock, auto/manual-bump rule, unique backstop, first-missing gap warning.
- `service/VoucherLedgerValidator` — shared Account-group checks (cash-or-bank membership) used by the intake.
- `entity/Voucher`, `entity/VoucherEntry`, `entity/VoucherCounter` — the recorded voucher, its double-entry lines, and its number counter.
- `entity/CustomVoucherSeries` — bill series per shop × type × code × FY with `next_number` (supported types: SALES, PAYMENT, RECEIPT, CONTRA, JOURNAL).
- `entity/VoucherType` — the voucher kinds with their legacy-counter prefixes (`PY`, `RC`, `CNT`, `JN`, `SV`); custom series supported for SALES, PAYMENT, RECEIPT, CONTRA, JOURNAL.
- `util/FinancialYear` — FY derivation from `voucher_date` (Kolkata Apr-1) and `YYYY-YY` validation.
- `dto/posting/PostingRequest`, `PostingEntry`, `PostingResult` — the internal model a validated voucher converts into before posting (`seriesCode` + `manualBillNo` ride along), and the posted outcome (voucher + display number + gap warning).
- `mapper/VoucherPostingMapper` — posted voucher → contract `VoucherResponse`, including the series display number and `warnings.first_missing_bill_no` when a gap is detected.
- `repository/*` — Spring Data JDBC repos, including `LedgerEntryWithBalanceProjection` (voucher lines joined with ledger + balance, used by reports).

## Dependencies

- `core/account_master/` (ledgers: every entry posts against a ledger here), `common/` (`ShopAwareEntity`, `Result`), `shop/` (RLS on every posting transaction), `error/` (invalid-book / not-found exceptions during validation).

## How to Extend

To let the firm record a new kind of business transaction as a voucher, add a voucher type:

1. Add the type + prefix in `VoucherType` (e.g. a new `PYMT-`/`SR-` series).
2. Add a `process<X>` method in `VoucherIntakeService` that validates (reject invalid ledgers/amounts with `LekhaiClientException`s) and converts to a `PostingRequest` via the shared `convertDirectional` / `convertTwoSided` helpers — no new class.
3. Wire the endpoint in `VoucherController` and expose it via `lekhaiapispec`.
4. Verify counter behaviour in `VoucherCounterService` and add a migration only if the new type needs its own column/counter seeding.

## Deep Dive

> Read `DETAILS.md` when: changing the posting algorithm, the processor template, counter logic, or adding a voucher type end-to-end.