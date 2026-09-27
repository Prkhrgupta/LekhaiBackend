# Dual numbering for sales: voucher_number plus bill_no per sale series

Sales post one **Voucher** (`VoucherType=SALES`) with an internal continuous **Voucher Number**, plus one **Sale Invoice** with a legal **Bill Number** scoped to its **Sale Series** (GST, Retail, CN, DN) and reset each financial year. Tax is stored as a per-line **Tax Snapshot** with a separate GST filing table for **E-Invoice** / **E-Way Bill**.

## Decisions

- One `sale_series` master row per bill series: `series_code, voucher_type, prefix, next_bill_no, current_fy, fy_reset, branch_no, default_bank_ledger_id, default_sale_ledger_id`. `GST -> SALES`, `CN -> CREDIT_NOTE`, `DN -> DEBIT_NOTE`. Rejected: `invoice_type` as a parallel counter to `voucher_counter`.
- Link by stable keys, never display strings: `sale_invoice_header.voucher_id UNIQUE FK -> voucher`, `sale_invoice_line.header_id FK`, `sale_line_tax.line_id FK`. Rejected: joining on `vno` / `bill` string (`GST/2`).
- Single-rate is a degenerate multi-rate: header totals always `= SUM(lines) = SUM(tax lines)`. Each line carries `gst_rate_snapshot + taxable_snapshot`; each tax split lives in `sale_line_tax(tax_type, rate, amount, tax_ledger_id)`. Rejected: header-only `cgstp/cgsta` flat columns.
- All party references are FKs, nullable: `party_ledger_id -> ledger`, `broker_id -> broker`, `transport_id -> transport`, `cash_ledger_id -> ledger`. Legacy `0` / empty migrates to `NULL`.
- Negative stock allowed with warning only. No `CHECK(qty >= 0)`; v1 reports stock as `opening + SUM(lines)`.
- GST filing separated from day one (per Q6): `sale_invoice_header` holds commercial + tax totals only; `sale_gst_filing(header_id, irn, ack_no, ack_dt, ewb_no, vehicle_no, gr_no, transporter_id, status)` holds **E-Invoice** / **E-Way Bill** lifecycle so retries don't rewrite the bill.
- `fy_reset=true` on `sale_series`: on reserve, if `current_fy != todayFY` (Apr-Mar, same rule as `VoucherPostingService` JWT FY), reset `next_bill_no=1`. Uniqueness is `(shop_code, series_id, financial_year, bill_no)`.

## Considered Options

- Single counter doing both jobs (drop `voucher_counter` for sales) — rejected: loses continuous audit trail when bill series resets each April.
- Nullable `irn/ack/ewb` columns on the header only — rejected per Q6 in favour of a separate filing table so cancel/extend history survives.
- Live tax lookup at report time from `SaleLedgerSetting` — rejected: master edits would rewrite filed GST history.

## Consequences

- Posting reserves two numbers atomically (`voucher_counter` + `sale_series`) under `SELECT FOR UPDATE` in one `@ShopContextTransactional`; failure rolls back both.
- Reports and GST filings read snapshots, never masters.
- Stock negatives need a warning path in intake, not a DB constraint.

## Gotchas — discuss before implementation

1. Dual-number lockstep and gap handling on rollback / retry.
2. Credit-note linkage to original bill (`against_header_id`) not in v1 scope — needed for GST CN filing.
3. Header-totals invariant enforced in service, not DB constraints.
4. EWB/IRN calls must not hold the posting DB tx open; use outbox or follow-up update to `sale_gst_filing`.
5. Legacy `sale_file` / `stock_files` migration: `vno -> voucher_id`, `prod -> stock_item_id`, `0 -> NULL`, `bill string -> series_id + bill_no`.
6. Inter-state rule (CGST+SGST vs IGST) decided by shop-state vs party-state comparison at posting.
7. Round-off / freight / TDS / TCS ledgers resolved from `GeneralLedgerSetting` at posting and snapshotted into `voucher_entry`.
