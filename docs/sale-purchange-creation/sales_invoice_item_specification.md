# Sales Invoice Item Specification

## Database Schema

```sql
Table sales_invoice_item {
  id bigint [pk, increment]
  shop_code int [not null, note: 'Tenant boundary']
  sales_invoice_id bigint [not null, ref: > sales_invoice.id, note: 'Links to the parent invoice header']
  
  srno int [not null, note: 'Line number for ordering (1, 2, 3...)']
  
  -- Item Details & Snapshots (Frozen at billing time)
  stock_item_id bigint [not null]
  item_name_snapshot varchar(200) [not null, note: 'Name exact at time of billing']
  hsn_code_snapshot varchar(10) [not null, note: 'HSN code required for GSTR-1 Table 12']
  
  godown_id bigint [note: 'Optional: Which warehouse/location this stock leaves from']

  -- Generic Quantities
  quantity numeric(18,3) [not null, default: 0, note: 'Primary billed quantity']
  uom_snapshot varchar(10) [not null, note: 'Primary Unit of Measure (e.g., BOX, KG, MTR)']
  
  alternate_quantity numeric(18,3) [not null, default: 0, note: 'Secondary tracking (e.g., if selling 2 Boxes, alternate could be 20 Pcs)']
  alternate_uom_snapshot varchar(10) [note: 'Secondary Unit of Measure (e.g., PCS, GM)']
  
  -- Pricing & Discounts
  rate numeric(18,2) [not null, note: 'Rate applied per primary UOM']
  gross_amount numeric(18,2) [not null, note: 'Calculated: (quantity * rate)']
  discount_pct numeric(18,2) [not null, default: 0]
  discount_amount numeric(18,2) [not null, default: 0]
  
  -- Taxable Base & GST
  taxable_value numeric(18,2) [not null, note: 'Calculated: (gross_amount - discount_amount)']
  gst_rate_snapshot numeric(18,2) [not null, note: 'e.g., 18.00, 12.00, 5.00']
  
  remarks text [note: 'Optional line-level narration or serial numbers']
}
```

## Application Logic & Implementation Rules

* **Data Immutability (Snapshots):**
  Always copy the `item_name`, `hsn_code`, `uom`, and `gst_rate` from the stock/item master at the exact time of billing. If the master data changes in the future, this historical invoice line MUST remain unchanged to satisfy GST audit and reprint requirements.

* **Line-Level Calculations:**
  The backend must calculate line values precisely in this order before saving:
  1. `gross_amount` = `quantity` * `rate`
  2. `taxable_value` = `gross_amount` - `discount_amount`
  
  *Note: If `discount_pct` is provided by the UI, the backend must calculate `discount_amount` as (`gross_amount` * `discount_pct` / 100) and store the absolute amount.*

* **Generic Dual-Quantity Handling:**
  The schema supports both single-UOM and dual-UOM items.
  * *Single UOM (e.g., Electronics):* `quantity` = 1, `uom_snapshot` = 'PCS', `alternate_quantity` = 0.
  * *Dual UOM (e.g., FMCG Box/Pieces):* `quantity` = 5, `uom_snapshot` = 'BOX', `alternate_quantity` = 50, `alternate_uom_snapshot` = 'PCS'. This provides exact conversion data to the inventory deduction service.

* **GSTR-1 Compliance (Table 12):**
  The `hsn_code_snapshot`, `taxable_value`, and `gst_rate_snapshot` form the foundation for generating the mandatory HSN Summary required by the Indian GST portal during monthly filing.