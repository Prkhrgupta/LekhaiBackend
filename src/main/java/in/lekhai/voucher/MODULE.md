# voucher

Voucher processing and posting engine: payment, receipt, contra, journal vouchers.

## Public API

- `controller/VoucherController` — voucher REST endpoints.
- `service/VoucherProcessor<T>` — abstract template: `validate()` → `covertToVoucherPostRequest()` → `post()`. Concrete processors: `PaymentVoucherService`, `ReceiptVoucherService`, `ContraVoucherService`, `JournalVoucherService`.
- `service/posting/VoucherPostingService` — shared posting engine (double-entry ledger writes).
- `service/VoucherCounterService` — per-voucher-type number counters.
- `entity/Voucher`, `VoucherEntry`, `VoucherCounter` — persistence entities.
- `entity/VoucherType` — enum with prefix codes (`PY`, `RC`, `CNT`, `JN`).
- `dto/posting/PostingRequest`, `PostingEntry` — internal posting model.
- `mapper/VoucherPostingMapper` — entity ↔ contract response mapping.
- `repository/VoucherRepository`, `VoucherEntryRepository`, `VoucherCounterRepository`, `LedgerEntryWithBalanceProjection`.

## Dependencies

- `common/` (`ShopAwareEntity`, `Result`), `error/`, `core/account_master/` (ledger), `shop/`

## How to Extend

Add a voucher type:
1. Extend `VoucherProcessor<T>` and implement `validate()` + `covertToVoucherPostRequest()`.
2. Add a value to `VoucherType` (with prefix).
3. Wire the endpoint in `VoucherController` (via generated contract).
4. Optional: verify counter behavior in `VoucherCounterService`.

## Deep Dive

> Read `DETAILS.md` only when: modifying posting algorithm, template contract, or counter logic.