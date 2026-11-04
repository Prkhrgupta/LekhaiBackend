# salesinvoice — details

## Create flow (`SalesInvoiceService.create`)

1. `SalesInvoicePreparationService.prepare`: reject `ship_to_id` and empty items; party must be under Sundry Debtors, Sundry Debtors (Others) or Cash in Hand; place of supply = request, else party state, else 422; seller state from `shops.state_code`; `intra/inter` = seller `state.gst_code` vs place of supply.
2. Item snapshots: name, HSN, category name, primary UOM, GST/cess from the commodity (422 if no commodity). Cost rate = `purchase_price`, else `opening_rate`.
3. `InvoiceCalculator`: gross = qty × rate; discount = gross × pct (2dp); taxable = gross − discount. GST is summed **unrounded** per slab, then rounded once per tax row. Zero-amount rows are dropped. Net = taxable + taxes rounded to the rupee; `round_off` is signed.
4. Ledgers: `SalesInvoiceLedgerResolver` maps `IN_STATE`/`OUT_STATE` + GST rate to the sale and CGST/SGST/IGST ledgers (422 when missing). CESS posts to `output_cess_ledger_id`. Round-off ledger is required only when `round_off != 0`.
5. `SalesInvoicePostingBuilder`: Dr party (net); Cr sale ledgers grouped by ledger; Cr each tax row; round-off Cr (rounded up) or Dr (rounded down). The party debit is asserted equal to `net_amount`.
6. `VoucherPostingService.post` (type SALES + series) allocates the bill number; `SalesInvoicePersistenceService` writes header/items/taxes; `StockLedgerService.recordSale` writes `SALE` out rows.

## Rules

- `supply_type`: GSTIN present → `B2B`; else inter-state with taxable > ₹1,00,000 → `B2CL`; else `B2CS`.
- Negative stock is never blocked: a `NEGATIVE_STOCK` warning is returned and logged at WARN.
- `total_add_charges` is always 0 (deferred). `is_rcm` is always false.
- Preview's `bill_no` is a peek at the series' next number (or the manual number); nothing is reserved.
- `total_pcs` sums quantities of lines whose UOM code is `PCS` (cloth-shop v1).

## Data

Migration `V18__create_sales_invoice_tables.sql`: `shops.state_code` (backfilled from GSTIN prefix), `sales_invoice`, `sales_invoice_item`, `sales_invoice_tax`, `stock_ledger`, all with shop isolation policies. Unique `voucher_id` and `(shop_code, series_id, financial_year, bill_no)`.
