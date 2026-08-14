-- Indexes to support paginated ledger reports with running balance.
-- Covers the RLS shop_code filter, the ledger_id filter and the
-- per-voucher ordering used by the ledger report query. The voucher_date
-- ordering lives on the voucher table and cannot be included here.
CREATE INDEX idx_voucher_entry_ledger_line
    ON voucher_entry (shop_code, ledger_id, voucher_id, line_number, id);
