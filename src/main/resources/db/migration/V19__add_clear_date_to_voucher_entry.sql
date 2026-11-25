ALTER TABLE voucher_entry ADD COLUMN IF NOT EXISTS clear_date DATE;
CREATE INDEX IF NOT EXISTS idx_voucher_entry_clear_date ON voucher_entry (shop_code, ledger_id, clear_date);
