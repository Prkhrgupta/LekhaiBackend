# voucher — details

## Files & Roles

| File | Role |
|---|---|
| `controller/VoucherController.java` | Endpoints for create/list voucher plus series CRUD; implements generated `*Api`. |
| `service/VoucherIntakeService.java` | Single intake for all voucher kinds. Public `processPayment/Receipt/Contra/Journal`: per-kind validation + conversion to `PostingRequest` → `VoucherPostingService.post` → mapped `VoucherResponse`. Shared private helpers own the duplicated guards (null/date/entries/balance) and the two converters (`convertDirectional` for payment/receipt, `convertTwoSided` for contra/journal). |
| `service/VoucherLedgerValidator.java` | Shared Account-group checks: ledger exists, cash-or-bank membership. |
| `service/posting/VoucherPostingService.java` | Core engine: builds `Voucher` + `VoucherEntry` rows, assigns counters, persists under `@ShopContextTransactional`. |
| `service/VoucherCounterService.java` | Generates sequential voucher numbers per type via atomic `reserveNextNumber` (fetch + advance + save in one call, inside the posting transaction). |
| `service/VoucherSeriesService.java` | SALES series CRUD with FY + 16-char + immutability rules; maps to contract `CustomVoucherSeries`. |
| `service/SeriesReserveService.java` | Dormant FY-scoped allocator: locks series `FOR UPDATE`, applies auto/manual-bump, returns first-missing warning. |
| `util/FinancialYear.java` | Derives FY string from `voucher_date` (Apr-1 cutoff) and validates `YYYY-YY`. |
| `mapper/VoucherPostingMapper.java` | Maps posted `Voucher` → contract `VoucherResponse`. |
| `entity/Voucher.java` | Voucher header (type, date, party, totals, status, optional series link). Shop-scoped via RLS, not `ShopAwareEntity`. |
| `entity/VoucherEntry.java` | Double-entry line (ledger + debit/credit + amount). |
| `entity/VoucherCounter.java` | Per-type counter row. |
| `entity/CustomVoucherSeries.java` | Series row per shop × type × code × FY. Extends `ShopAwareEntity`. |
| `entity/VoucherType.java` | Enum: PAYMENT, RECEIPT, CONTRA, JOURNAL, SALES + prefixes. |
| `dto/posting/PostingRequest.java` | `record`: type, date, entries list, reference info. |
| `dto/posting/PostingEntry.java` | ledger, debit/credit amounts, narration. |
| `repository/*.java` | Spring Data JDBC repos + `LedgerEntryWithBalanceProjection` (JOIN ledger + balance for reports). |

## Behavioral Notes

- Every voucher MUST balance debits == credits before post; `VoucherPostingService` enforces consistency (or relies on processor `validate`).
- Posting mutates ledger balances — always inside `@ShopContextTransactional` so RLS is active and rows are shop-scoped.
- Counters must be atomic (row-level counter + concurrency-safe update). Do not guess next number client-side.
- Series numbering is FY-scoped from `voucher_date` (Kolkata Apr-1); missing FY rows raise `SERIES_FY_MISSING` (422).
- Printed bill length `prefix+number+suffix` must fit 16 chars; prefix/suffix freeze after the first voucher in that series-FY.
- `VoucherEntry` ledger references must exist in `core/account_master` ledger; validation in processors checks this.

## Adding a Voucher Type — Full Walkthrough

1. Add enum constant + prefix in `VoucherType`.
2. Add `process<X>` in `VoucherIntakeService`: validate (throw appropriate `LekhaiClientException`s for invalid books/ledgers/amounts), reusing `requireVoucherDate`, `requireEntriesPresent`, `sumEntries` with the matching `LedgerKind`, and `requireBalanced` where both sides are user-supplied.
3. Convert with `convertDirectional` (one cash-or-bank side + item lines) or `convertTwoSided` (explicit debit/credit lines).
4. Register the endpoint in `VoucherController` and expose via `lekhaiapispec`.
5. Add migration if the new type needs its own table column or counter row seeding.