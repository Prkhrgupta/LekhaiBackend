# Stock Ledger Specification

## Database Schema

```sql
Table stock_ledger {
  id bigint [pk, increment]
  shop_code int [not null, note: 'Tenant boundary']
  
  -- Master Links
  stock_item_id bigint [not null, ref: > stock_item_master.id]
  godown_id bigint [note: 'Optional: To track stock at specific locations (e.g., Factory vs Godown)']
  
  -- Transaction Context
  transaction_date date [not null, note: 'Crucial for chronological stock valuation (FIFO)']
  voucher_id bigint [note: 'Links to parent voucher. Nullable ONLY for Opening Balances']
  transaction_type varchar(30) [not null, note: 'OPENING, SALE, PURCHASE, PROD_IN, PROD_OUT, ADJ']
  
  -- Generic Inward Movement (Purchases, Production In, Sales Returns)
  in_qty numeric(18,4) [not null, default: 0, note: 'Primary Unit Quantity']
  in_alt_qty numeric(18,4) [not null, default: 0, note: 'Secondary Unit Quantity']
  in_rate numeric(18,4) [not null, default: 0, note: 'Rate per Primary Unit']
  in_value numeric(18,2) [not null, default: 0, note: 'Total value of inward movement']
  
  -- Generic Outward Movement (Sales, Purchase Returns, Raw Material Consumption)
  out_qty numeric(18,4) [not null, default: 0]
  out_alt_qty numeric(18,4) [not null, default: 0]
  out_rate numeric(18,4) [not null, default: 0]
  out_value numeric(18,2) [not null, default: 0]
  
  created_at timestamp [not null, default: 'CURRENT_TIMESTAMP']
  
  indexes {
    (shop_code, stock_item_id, transaction_date) [note: 'Optimizes date-filtered stock summaries']
    (shop_code, voucher_id) [note: 'Required for fast voucher rollback/cancellations']
  }
}
```

## Application Logic & Implementation Rules

* **UOM Independence:**
  This table is strictly quantitative. It does not store `uom_id` or UOM strings. The `in_qty` and `out_qty` columns implicitly represent the `primary_uom` defined in the `stock_item_master`, and the `_alt_` columns represent the `alternate_uom`. The UI will perform a `JOIN` to the master to display the correct labels (e.g., "Boxes" or "Kgs") next to these numbers.
* **Separation of Inward and Outward:**
  Never insert negative numbers into this table. 
  * If stock enters the business (Purchase), put positive numbers in `in_qty`. 
  * If stock leaves the business (Sale), put positive numbers in `out_qty`.
  * This allows the backend to calculate closing stock instantly using: `SUM(in_qty) - SUM(out_qty)`.
* **Chronological Integrity for Valuation:**
  The `transaction_date` is critical. If your application calculates inventory value using the FIFO (First-In-First-Out) or Moving Average method, the calculation engine will order the rows by `transaction_date` (and then `id`) to determine the exact value of the remaining stock on any given day.
* **Handling Voucher Cancellations:**
  If a user cancels a Sales Invoice, you should not run a `DELETE` command on the `stock_ledger` row. Instead, you have two safe options:
  1. Insert a reversal row with the same `voucher_id` but opposite values (e.g., put the canceled `out_qty` into the `in_qty` column as a "return").
  2. Or, if soft-deleting, ensure your stock summary queries are strictly filtering out canceled voucher states. 
* **Opening Balances:**
  When a user sets up their software for the first time, an opening balance is posted here with `transaction_type = 'OPENING'` and `voucher_id = null`. This acts as the baseline for all future calculations.