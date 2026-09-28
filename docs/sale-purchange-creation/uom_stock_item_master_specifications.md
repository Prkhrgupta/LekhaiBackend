# Unit of Measure (UOM) & Item Master Specifications

## 1. New Table: `uom_master`

```sql
Table uom_master {
  id bigint [pk, increment]
  shop_code int [not null, note: '0 for global system units, or specific tenant ID for custom units']
  
  unit_name varchar(50) [not null, note: 'User-friendly name, e.g., "Gunny Bag", "Strips"']
  gst_uqc_code varchar(10) [not null, note: 'Official government code, e.g., "BAG", "NOS", "OTH"']
  
  created_at timestamp [not null, default: 'CURRENT_TIMESTAMP']
}
```
* **Data Seeding:** On application startup, pre-fill this table with global records (`shop_code = 0`). Example: `unit_name = 'Pieces', gst_uqc_code = 'NOS'`.

## 2. Refactoring `stock_item_master` (Existing Table)

To make your software support all industries, remove hardcoded unit columns and replace them with generic foreign keys and conversion factors.

### What to REMOVE from your existing table:
```sql
-- DELETE THESE COLUMNS:
rate_per VARCHAR(10) CHECK (rate_per IN ('PCS', 'METER'))
opening_pcs NUMERIC(14, 2)
opening_meter NUMERIC(14, 3)
```

### What to ADD to your existing table:
```sql
-- ADD THESE COLUMNS:
-- UOM Mappings
primary_uom_id bigint [not null, ref: > uom_master.id, note: 'e.g., ID for BOX']
alternate_uom_id bigint [note: 'Nullable if item only has one unit']

-- Conversion Logic
conversion_factor numeric(18,4) [default: 0, note: 'How many Alternate UOM inside 1 Primary UOM. e.g., 10 (1 Box = 10 Pcs)']

-- Generic Opening Balance
opening_qty numeric(18,4) [default: 0, note: 'Tracks Primary Unit']
opening_alt_qty numeric(18,4) [default: 0, note: 'Tracks Secondary Unit']
```