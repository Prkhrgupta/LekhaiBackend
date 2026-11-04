# salesinvoice

Creating a GST **Sales Invoice** (Tally "Sales Voucher"): it prices the lines, splits GST, posts the **Voucher**, freezes a snapshot of the bill, and records the stock leaving the shop. Terms (Voucher, Ledger, Account group) are defined in `CONTEXT.md`.

## Business Goal

One request turns a party, a date, a SALES series and a list of items into a numbered, balanced, immutable invoice. Preview runs the same maths without posting or reserving a bill number, so the UI never duplicates GST rules.

## Public API

- `controller/SalesInvoiceController` — implements the generated `SalesInvoiceApi`: `POST /sales-invoice` (201) and `POST /sales-invoice/preview`.
- `core/inventory_master/controller/StockItemController#getStockItemAvailability` — delegates to `StockLedgerService` (`GET /stock-item/{id}/availability`).
- `service/SalesInvoiceService` — `create` and `preview`, both `@ShopContextTransactional`.
- `service/SalesInvoicePreparationService` — read-only: validate, snapshot masters, calculate, resolve ledgers → `PreparedInvoice`.
- `service/StockLedgerService` — `getAvailability` (opening + in − out), `recordSale` (out rows).
- `calc/InvoiceCalculator` — pure BigDecimal GST maths (`InvoiceModels` holds its value objects).
- `mapper/SalesInvoiceMapper` — `PreparedInvoice` → contract responses.
- `entity/*` — `SalesInvoice`, `SalesInvoiceItem`, `SalesInvoiceTax`, `StockLedger` (immutable records with `withId`).

## Dependencies

`voucher/` (`VoucherPostingService`, series), `core/account_master/` (ledgers, sale/general ledger settings, states), `core/inventory_master/` (stock item, commodity, UOM, category), `core/domain/shop` (seller `state_code`), `shop/` (RLS), `error/`.

## How to Extend

- Cancel/list/detail: add endpoints in the apispec first; reuse `SalesInvoiceResponse`. Cancellation = status `CANCELLED`, reversal `voucher_entry` lines on the same voucher, `SALE_CANCEL` stock rows. Never delete.
- Ship-to: `ship_to_id` is rejected (422) until the ship-to table exists.
- E-way bill / broker / transport: add an optional object to the contract, store in `sales_invoice.metadata`.

## Deep Dive

> Read `DETAILS.md` when changing the calculation, posting lines, or stock valuation.
