# voucher

Recording and posting **every financial transaction of the firm** — the heart of the double-entry accounting engine. In Tally terms, these are the Voucher Entry screens (F4 Contra, F5 Payment, F6 Receipt, F7 Journal): the source documents from which all ledgers and reports are built.

## Business Goal

All money that moves into, out of, or between a shop's accounts is captured here as a voucher, then posted to the ledger in a way that keeps the books balanced. The voucher types map directly to real bookkeeping acts:

- **Payment** — money the firm pays out (to a supplier, expense, loan repayment).
- **Receipt** — money the firm receives (from a customer, advance, sale proceeds).
- **Contra** — a transfer between the firm's own cash & bank accounts (e.g. "cash deposited in bank"); no external party involved.
- **Journal** — adjustments and non-cash entries (opening balances, provisions, corrections), typically used by the accountant.

Each voucher is validated first, then converted into a set of double-entry lines (ledger + debit + credit + narration) and posted so that **total debits always equal total credits** — the invariant that keeps Lekhai's books accurate, exactly as Tally would enforce. Every posted voucher also receives a sequential, per-type number (e.g. `PY-0007`, `RC-0003`) so the firm's voucher trail is continuous and referenceable.

Posting mutates the ledgers' balances, so it happens atomically inside a shop-scoped transaction (RLS active) — a wrong step here corrupts the books, not just a single record.

## Public API

- `controller/VoucherController` — API surface to create and list vouchers (implements the generated contract).
- `service/VoucherProcessor<T>` — the template each voucher type follows: **validate → build posting request → post → respond**. Per-type processors: `PaymentVoucherService`, `ReceiptVoucherService`, `ContraVoucherService`, `JournalVoucherService`.
- `service/posting/VoucherPostingService` — the posting engine: builds the voucher + its entry rows, enforces the debits==credits rule, assigns counters, and persists under a shop-scoped transaction.
- `service/VoucherCounterService` — issues sequential voucher numbers per type.
- `entity/Voucher`, `entity/VoucherEntry`, `entity/VoucherCounter` — the recorded voucher, its double-entry lines, and its number counter.
- `entity/VoucherType` — the four voucher kinds with their prefixes (`PY`, `RC`, `CNT`, `JN`).
- `dto/posting/PostingRequest`, `PostingEntry` — the internal model a validated voucher converts into before posting.
- `mapper/VoucherPostingMapper` — posted voucher → contract response.
- `repository/*` — Spring Data JDBC repos, including `LedgerEntryWithBalanceProjection` (voucher lines joined with ledger + balance, used by reports).

## Dependencies

- `core/account_master/` (ledgers: every entry posts against a ledger here), `common/` (`ShopAwareEntity`, `Result`), `shop/` (RLS on every posting transaction), `error/` (invalid-book / not-found exceptions during validation).

## How to Extend

To let the firm record a new kind of business transaction as a voucher, add a voucher type:

1. Add the type + prefix in `VoucherType` (e.g. a new `PYMT-`/`SR-` series).
2. Create `<X>VoucherService extends VoucherProcessor<...>` — implement `validate` (reject invalid ledgers/amounts with `LekhaiClientException`s) and the conversion to a `PostingRequest`.
3. Wire the endpoint in `VoucherController` and expose it via `lekhaiapispec`.
4. Verify counter behaviour in `VoucherCounterService` and add a migration only if the new type needs its own column/counter seeding.

## Deep Dive

> Read `DETAILS.md` when: changing the posting algorithm, the processor template, counter logic, or adding a voucher type end-to-end.