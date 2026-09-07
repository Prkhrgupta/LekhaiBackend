# voucher — details

## Files & Roles

| File | Role |
|---|---|
| `controller/VoucherController.java` | Endpoints for create/list voucher; implements generated `*Api`. |
| `service/VoucherProcessor.java` | Abstract base. Template method `process(T)`: `validate` → `covertToVoucherPostRequest` → `voucherPostingService.post(postRequest)` → mapped `VoucherResponse`. |
| `service/PaymentVoucherService.java` | Payment-specific validate + conversion. |
| `service/ReceiptVoucherService.java` | Receipt-specific validate + conversion. |
| `service/ContraVoucherService.java` | Contra-specific validate + conversion. |
| `service/JournalVoucherService.java` | Journal-specific validate + conversion. |
| `service/posting/VoucherPostingService.java` | Core engine: builds `Voucher` + `VoucherEntry` rows, assigns counters, persists under `@ShopContextTransactional`. |
| `service/VoucherCounterService.java` | Generates sequential voucher numbers per type (e.g., `PY-0007`). |
| `mapper/VoucherPostingMapper.java` | Maps posted `Voucher` → contract `VoucherResponse`. |
| `entity/Voucher.java` | Voucher header (type, date, party, totals, status). Extends `ShopAwareEntity`. |
| `entity/VoucherEntry.java` | Double-entry line (ledger + debit/credit + amount). |
| `entity/VoucherCounter.java` | Per-type counter row. |
| `entity/VoucherType.java` | Enum: PAYMENT, RECEIPT, CONTRA, JOURNAL + prefixes. |
| `dto/posting/PostingRequest.java` | `record`: type, date, entries list, reference info. |
| `dto/posting/PostingEntry.java` | ledger, debit/credit amounts, narration. |
| `repository/*.java` | Spring Data JDBC repos + `LedgerEntryWithBalanceProjection` (JOIN ledger + balance for reports). |

## Behavioral Notes

- Every voucher MUST balance debits == credits before post; `VoucherPostingService` enforces consistency (or relies on processor `validate`).
- Posting mutates ledger balances — always inside `@ShopContextTransactional` so RLS is active and rows are shop-scoped.
- Counters must be atomic (row-level counter + concurrency-safe update). Do not guess next number client-side.
- `VoucherEntry` ledger references must exist in `core/account_master` ledger; validation in processors checks this.

## Adding a Voucher Type — Full Walkthrough

1. Add enum constant + prefix in `VoucherType`.
2. Create `XyzVoucherService extends VoucherProcessor<ContractDto>`: construct via the protected two-arg constructor.
3. Implement `validate(ContractDto)` — throw appropriate `LekhaiClientException`s for invalid books/ledgers/amounts.
4. Implement `covertToVoucherPostRequest(ContractDto)` returning a `PostingRequest`.
5. Register the endpoint in `VoucherController` and expose via `lekhaiapispec`.
6. Add migration if the new type needs its own table column or counter row seeding.