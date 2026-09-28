# Sales Invoice Tax Specification

## Database Schema

```sql
Table sales_invoice_tax {
  id bigint [pk, increment]
  shop_code int [not null, note: 'Tenant boundary']
  sales_invoice_id bigint [not null, ref: > sales_invoice.id, note: 'Links to Header, acting as a grouped summary']
  
  -- Grouped Tax Summary
  tax_type varchar(10) [not null, note: 'CGST, SGST, IGST, CESS']
  tax_rate numeric(18,2) [not null, note: 'e.g., 9.00 for CGST']
  tax_amount numeric(18,2) [not null, note: 'Rolled up total for this rate across all items']
  
  -- Core Accounting Linkage
  tax_ledger_id bigint [not null, note: 'The specific tax ledger to credit in voucher_entry']
}
```

## Application Logic & Implementation Rules

* **Header-Level Rollup (Grouping):**
  This table acts as a summary of taxes for the entire invoice. The backend logic MUST NOT insert a separate tax row for every single item. Instead, before saving, the system must group the line items by their `gst_rate_snapshot` and sum up the tax amounts.
  * *Example:* If an invoice has three items at 18% Local GST (9% CGST + 9% SGST), this table should only receive **two rows**: one for Total CGST @ 9% and one for Total SGST @ 9%.

* **Snapshot Principle for Ledgers:**
  The `tax_ledger_id` must be fetched from the user's GST settings at the exact moment of bill creation and saved permanently in this row. If the user later changes their default tax ledgers in the settings, this historical bill remains mapped to the original ledgers. This guarantees that historical edits/reprints don't accidentally shift balances across different ledgers.

* **Core Accounting Integration (`voucher_entry`):**
  Every row inserted into this `sales_invoice_tax` table directly translates to a **Credit** entry in the core double-entry system. 
  * *Mapping Flow:* When constructing the `voucher_entry` payload, iterate through the `sales_invoice_tax` rows. Post a Credit entry of `tax_amount` to the specific `tax_ledger_id`.

* **Handling Inter-state vs Intra-state:**
  The insertion logic must refer to the header's `place_of_supply` logic:
  * If Intra-state: Group items by (Rate / 2) and insert distinct `CGST` and `SGST` rows.
  * If Inter-state: Group items by (Full Rate) and insert `IGST` rows.

* **Rounding Considerations:**
  Always calculate the tax amounts precisely at the item level first, sum them up, and then apply standard 2-decimal rounding for the `tax_amount` stored in this table. Do not round at the item level before summing, as this will lead to cent-level mismatches in the grand total.