# Custom Voucher Series Specification

## Overview
The `custom_voucher_series` table acts as the configuration foundation for generating document sequences (e.g., Sales Invoices, Credit Notes) across the application. It manages continuous, gapless numbering per financial year, handles formatting according to Indian GST regulations, and stores flexible UI/ledger defaults using JSONB.

## Database Schema

```sql
Table custom_voucher_series {
  id bigint [pk, increment]
  shop_code int [not null, note: 'Tenant boundary for PostgreSQL RLS isolation']
  series_code varchar(30) [not null, note: 'e.g., GST, RETAIL, CN']
  voucher_type varchar(30) [not null, note: 'SALES, CREDIT_NOTE']
  
  -- Numbering Rules
  prefix varchar(10) [default: '', note: 'e.g., "GST/"']
  suffix varchar(10) [default: '', note: 'e.g., "/24-25"']
  number_padding int [not null, default: 4, note: 'Generates zero-padded sequence (e.g., 4 = 0001)']
  next_bill_no bigint [not null, default: 1]
  
  -- Fiscal Handling
  current_fy varchar(10) [not null, note: 'e.g., 2024-25']
  fy_reset bool [not null, default: true, note: 'Triggers reset of next_bill_no to 1 in April']
  
  -- Flexible Configuration
  metadata jsonb [note: 'Stores UI config, manual entry flags, print templates']
}
```

## Key Engineering Decisions

### 1. Multi-Tenancy
- **`shop_code`**: Acts as a strict tenant boundary. Since each branch operates as a distinct shop account in this architecture, sequences are securely isolated per shop.

### 2. CGST Rule 46(b) Enforcement & Padding
- **`number_padding`**: Standardizes visual consistency by zero-padding invoice numbers (e.g., `4` generates `0001`, `0012`).
- **Rule 46(b) Compliance**: Under Indian GST law, a tax invoice number must **not exceed 16 characters**. The application code MUST evaluate: `length(prefix) + number_padding + length(suffix) <= 16` before saving the configuration. This prevents generating illegal invoice numbers that would fail E-Invoice or E-Way Bill portal validations.

### 3. The `metadata` JSONB Payload
Instead of cluttering the schema with specific, optional configurations (like default cash ledgers or UI behaviors), we use a JSONB column. 

**Example Payload:**
```json
{
  "allow_manual_entry": true,
  "print_settings": {
    "template": "A4_TAX_INVOICE",
    "print_on_save": true
  },
  "default_ledgers": {
    "bank_ledger_id": 8493,
    "cash_ledger_id": 1002
  }
}
```

## Application Logic Requirements

### 1. Concurrency & Row Locking
To prevent duplicate invoice numbers in a multi-user environment, the backend service MUST use pessimistic row locking (`SELECT ... FOR UPDATE`) on the `custom_voucher_series` row when fetching and incrementing `next_bill_no`.

### 2. Handling Manual Bill Entries
If the UI allows a user to manually input a bill number (based on `allow_manual_entry: true` in metadata), the backend must safely handle the counter:
- If the user submits a manual `bill_no` that is `>= next_bill_no`, the system must update the counter to prevent future collisions: `next_bill_no = manual_bill_no + 1`.
- If the user is entering a backdated bill to fill a missing sequence gap (manual `bill_no` `< next_bill_no`), do not update the counter.

### 3. Detecting Missing Sequences (Gaps)
Instead of building a separate tracking table for missing bills, the application should dynamically detect gaps using PostgreSQL's `generate_series()` function. This is highly useful for GST audits.

**Example Gap Detection Query:**
```sql
SELECT s.i AS missing_bills 
FROM generate_series(
    1, 
    (SELECT max(bill_no) FROM sale_invoice_header WHERE series_id = :seriesId)
) s(i) 
WHERE NOT EXISTS (
    SELECT 1 
    FROM sale_invoice_header 
    WHERE bill_no = s.i AND series_id = :seriesId
);
```