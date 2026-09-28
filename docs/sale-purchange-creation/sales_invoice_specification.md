# Sales Invoice Specification

## Database Schema

```sql
Table sales_invoice {
  id bigint [pk, increment]
  shop_code int [not null, note: 'Tenant boundary for multi-tenancy']
  voucher_id bigint [not null, unique, ref: > voucher.id, note: '1-to-1 link to double-entry ledger impact']
  series_id bigint [not null, ref: > custom_voucher_series.id]

  -- Document Identifiers
  bill_no bigint [not null]
  bill_display varchar(16) [not null, note: 'Max 16 chars per CGST Rule 46(b)']
  financial_year varchar(10) [not null, note: 'e.g., 2024-25']
  bill_date date [not null]

  -- Party Details & Legal Snapshots (Frozen at billing time)
  party_ledger_id bigint [not null]
  party_name_snapshot varchar(100) [not null]
  party_gstin_snapshot varchar(15)
  billing_address_snapshot text
  shipping_address_snapshot text
  place_of_supply varchar(2) [not null, note: '2-digit state code, e.g., 27']

  -- GST Classification
  supply_type varchar(20) [not null, default: 'B2B', note: 'B2B, B2CL, B2CS, EXPORT, SEZ']
  is_rcm bool [not null, default: false]
  status varchar(20) [not null, default: 'ACTIVE', note: 'ACTIVE, CANCELLED']

  -- Financial Aggregates
  total_goods_value numeric(18,2) [not null, default: 0]
  total_discount numeric(18,2) [not null, default: 0]
  total_add_charges numeric(18,2) [not null, default: 0]
  total_taxable numeric(18,2) [not null, default: 0]

  -- Tax Totals
  cgst_amt numeric(18,2) [not null, default: 0]
  sgst_amt numeric(18,2) [not null, default: 0]
  igst_amt numeric(18,2) [not null, default: 0]
  cess_amt numeric(18,2) [not null, default: 0]

  round_off numeric(18,2) [not null, default: 0]
  net_amount numeric(18,2) [not null, default: 0]

  -- Extensibility & Notes
  metadata jsonb [note: 'Stores optional broker_id, transport_id, vehicle_no, UI flags']
  remarks text
  created_at timestamp [not null]
  updated_at timestamp [not null]

  indexes {
    (shop_code, series_id, financial_year, bill_no) [unique]
    (shop_code, bill_date)
    (shop_code, party_ledger_id)
  }
}
```

## Application Logic & Implementation Rules

* **Execution Order in Database Transaction:**
  1. Insert `voucher` -> obtain `voucher_id`.
  2. Insert `sales_invoice` using `voucher_id` -> obtain `id` (`header_id`).
  3. Insert line items into `sales_invoice_item` using `header_id`.
  4. Insert taxes into `sales_invoice_tax`.
  5. Post ledger balances into `voucher_entry` using `voucher_id`.

* **Immutability of Historical Bills:**
  Always pull party name, GSTIN, and addresses from the master at insertion and freeze them in `_snapshot` fields. Never use a `JOIN` to party masters when reprinting bills or exporting audit records.

* **Auto-Classification of `supply_type`:**
  * If `party_gstin_snapshot` is present -> `B2B`.
  * If unregistered and Interstate with taxable value > 1,00,000 -> `B2CL`.
  * If unregistered and Intra-state OR Interstate <= 1,00,000 -> `B2CS`.

* **Zero Hard-Deletes:**
  Never run `DELETE` on this table. To cancel an invoice, set `status = 'CANCELLED'`, zero out inventory impacts, and post a reversal entry to `voucher_entry`. The row must remain to maintain the continuous numbering sequence required by GST audits.

* **Header-Ledger Validation:**
  Before committing the transaction, assert that `net_amount` equals the sum of the Debit amounts posted to the party ledger in `voucher_entry`. Any mismatch indicates a rounding or calculation bug and must abort the transaction.