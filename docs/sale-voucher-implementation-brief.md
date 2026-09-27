# Sale Voucher — implementation brief (grilling output, no code yet)

Source: grilling rounds Q1-Q11. ADR: `docs/adr/0002-sale-voucher-dual-numbering.md`. Glossary: `CONTEXT.md`.
Do NOT implement until user explicitly asks.

## Choices made

- Q1/Q8: keep `voucher_counter(voucher_type)` for internal **Voucher Number**; add `sale_series` master for legal **Bill Number** + metadata (`prefix, branch_no, default_bank_ledger_id, default_sale_ledger_id, fy_reset`). `GST -> SALES`, `CN -> CREDIT_NOTE`, `DN -> DEBIT_NOTE`.
- Q2: link by `voucher_id FK`, never `vno` / `bill` string. Display `bill_display` derived.
- Q3/Q10: support single + multi-rate with per-line **Tax Snapshot** (`sale_invoice_line` + `sale_line_tax`); header totals `= SUM(lines)`.
- Q4: `party, broker, transport, cash` are nullable FKs to `ledger / broker / transport`. Legacy `0` -> `NULL`.
- Q5: negative stock allowed, warn only. No DB check constraint.
- Q6: separate `sale_gst_filing` table from v1 for **E-Invoice** / **E-Way Bill** (IRN, ack, EWB, vehicle, GR, status) so retries/audit don't rewrite the bill.
- Q9: `fy_reset=true` on `sale_series` with `current_fy`; reset `next_bill_no=1` each April. Unique `(shop_code, series_id, financial_year, bill_no)`.
- Legacy names retired: `invoice_type -> Sale Series`, `sale_file -> Sale Invoice`, `stock_files -> Sale Line`.

## Pending / open before build

1. Bill display format: `{prefix}/{bill_no}` vs `{branch}/{prefix}/{bill_no}/{FY}`.
2. Credit-note linkage: `against_header_id` needed for GST CN filing? Out of v1?
3. Cash-sale path: does `cash_ledger_id` create an extra `voucher_entry` leg in the same tx?
4. Inter-state detection: shop-state vs party-state source of truth (ledger address vs GSTIN)?
5. EWB/IRN trigger: sync on post vs async outbox? Who calls GSP?
6. Stock valuation method for `amount` (rate * qty - discount) and `rate_per` (Pcs/Mtr) canonical values.
7. Migration mapping for legacy `goods/less/add/taxable/cgst/sgst/igst/rof/net/rem1-3` to new columns.

## What an implementing agent needs

- Read first: `CONTEXT.md`, `src/main/java/in/lekhai/voucher/MODULE.md` + `DETAILS.md`, `core/account_master/MODULE.md` (`SaleLedgerSetting`, `GeneralLedgerSetting`, `Ledger`, `Broker`, `Transport`), `core/inventory_master/MODULE.md` (`StockItem`, `Commodity`), `V15__create_voucher_related_tables.sql`, `VoucherPostingService`, `VoucherCounterService`, `VoucherIntakeService`.
- Contracts: never hand-write DTOs; new endpoints/requests go via `lekhai-apispec` (`in.lekhai.contract.*`).
- Layering: controller thin -> service -> Spring Data JDBC repo. Errors via `LekhaiException` / `LekhaiClientException`, never raw `RuntimeException`.
- Tenancy: every new entity extends `ShopAwareEntity`; never set `shop_code` manually; all writes under `@ShopContextTransactional` with RLS; UTC dates.
- Migration: append-only Flyway `V17__...sql`, never edit applied migrations. Include `shop_code DEFAULT current_setting('app.shop_code')`, RLS policy calls, `NUMERIC(18,2)` for money, FKs + unique keys below.
- Posting flow (new + existing rows in one tx):
  1. Validate party/broker/sale-ledger/stock items exist; date inside JWT financial year; lines non-empty; `header totals = SUM(lines)`.
  2. `SELECT FOR UPDATE` lock `voucher_counter(SALES)` + `sale_series(series_code)`; FY-reset check on series.
  3. Insert `voucher(SALES, voucher_number, date, narration)`; insert `voucher_entry` legs: `Dr party net`, `Cr sale taxable`, `Cr CGST/SGST/IGST/Cess/Freight ledgers` (from `SaleLedgerSetting` + `GeneralLedgerSetting`), `Dr/Cr roundOff`.
  4. Insert `sale_invoice_header(voucher_id, series_id, bill_no, snapshots of totals + party/broker/sale-ledger refs)`.
  5. Insert `sale_invoice_line` rows + `sale_line_tax` rows with frozen `rate/amount/ledger_id`.
  6. Insert `sale_gst_filing(header_id, status=PENDING)`; external IRN/EWB fill it later, never block step 3-5.
  7. Advance both counters; return bill + voucher numbers. Warn (not fail) on negative stock.
- Tests: `<Class>Test` under `src/test/java/in/lekhai/` with AssertJ; TestContainers for DB tests; cover dual-number rollback, FY reset, multi-rate sum, snapshot immutability, negative-stock warn.

## dbdiagram.io schema (paste as-is)

```dbml
Table voucher {
  id bigint [pk, increment]
  shop_code int [not null]
  voucher_type varchar(30) [not null, note: 'SALES, CREDIT_NOTE, DEBIT_NOTE, existing']
  voucher_number bigint [not null, note: 'internal, never reset']
  voucher_date date [not null]
  narration text
  created_at timestamp [not null]
  updated_at timestamp [not null]
}

Table voucher_entry {
  id bigint [pk, increment]
  shop_code int [not null]
  voucher_id bigint [not null, ref: > voucher.id]
  ledger_id bigint [not null]
  line_number int [not null]
  debit_amount numeric(18,2) [not null, default: 0]
  credit_amount numeric(18,2) [not null, default: 0]
  remarks text
}

Table voucher_counter {
  id bigint [pk, increment]
  shop_code int [not null]
  voucher_type varchar(30) [not null]
  next_number bigint [not null, default: 1]
}

Table sale_series {
  id bigint [pk, increment]
  shop_code int [not null]
  series_code varchar(30) [not null, note: 'GST, RETAIL, CN, DN']
  voucher_type varchar(30) [not null, note: 'GST->SALES, CN->CREDIT_NOTE']
  prefix varchar(20) [not null, note: 'GST']
  next_bill_no bigint [not null, default: 1]
  current_fy varchar(10) [not null, note: 'FY25, Apr-Mar']
  fy_reset bool [not null, default: true]
  branch_no varchar(20)
  default_bank_ledger_id bigint
  default_sale_ledger_id bigint
}

Table sale_invoice_header {
  id bigint [pk, increment]
  shop_code int [not null]
  voucher_id bigint [not null, unique, ref: > voucher.id]
  series_id bigint [not null, ref: > sale_series.id]
  bill_no bigint [not null]
  bill_display varchar(50) [not null, note: 'GST/2']
  financial_year varchar(10) [not null]
  bill_date date [not null]
  party_ledger_id bigint [not null]
  broker_id bigint
  sale_ledger_id bigint [not null]
  transport_id bigint
  cash_ledger_id bigint
  goods numeric(18,2) [not null]
  less_discount numeric(18,2) [not null, default: 0]
  add_charges numeric(18,2) [not null, default: 0]
  taxable numeric(18,2) [not null]
  cgst_amt numeric(18,2) [not null, default: 0]
  sgst_amt numeric(18,2) [not null, default: 0]
  igst_amt numeric(18,2) [not null, default: 0]
  cess_amt numeric(18,2) [not null, default: 0]
  round_off numeric(18,2) [not null, default: 0]
  net numeric(18,2) [not null]
  remarks text
}

Table sale_invoice_line {
  id bigint [pk, increment]
  shop_code int [not null]
  header_id bigint [not null, ref: > sale_invoice_header.id]
  voucher_id bigint [not null, ref: > voucher.id]
  srno int [not null]
  stock_item_id bigint [not null]
  qty_pcs numeric(18,3) [not null, default: 0]
  qty_meter numeric(18,3) [not null, default: 0]
  rate_per varchar(10) [not null, note: 'Pcs, Mtr']
  rate numeric(18,2) [not null]
  discount_pct numeric(18,2) [not null, default: 0]
  amount numeric(18,2) [not null]
  gst_rate_snapshot numeric(18,2) [not null]
  taxable_snapshot numeric(18,2) [not null]
}

Table sale_line_tax {
  id bigint [pk, increment]
  shop_code int [not null]
  line_id bigint [not null, ref: > sale_invoice_line.id]
  tax_type varchar(10) [not null, note: 'CGST, SGST, IGST, CESS']
  rate numeric(18,2) [not null]
  amount numeric(18,2) [not null]
  tax_ledger_id bigint [not null]
}

Table sale_gst_filing {
  id bigint [pk, increment]
  shop_code int [not null]
  header_id bigint [not null, unique, ref: > sale_invoice_header.id]
  irn text
  ack_no text
  ack_dt timestamp
  ewb_no text
  ewb_valid_till timestamp
  vehicle_no varchar(20)
  gr_no varchar(30)
  transporter_id bigint
  status varchar(20) [not null, default: 'PENDING', note: 'PENDING, ACTIVE, CANCELLED']
}

Ref: sale_invoice_header_series_unique {
  // enforced in DDL as UNIQUE (shop_code, series_id, financial_year, bill_no)
}
```
