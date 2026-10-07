-- Custom voucher series per shop x voucher type x series code x financial year.
-- Supports SALES bill series (e.g. GST, RETAIL) with per-FY numbering.
CREATE TABLE custom_voucher_series (
    id BIGSERIAL PRIMARY KEY,
    shop_code INTEGER NOT NULL
        DEFAULT current_setting('app.shop_code', false)::INTEGER,
    voucher_type VARCHAR(30) NOT NULL,
    series_code VARCHAR(30) NOT NULL,
    financial_year VARCHAR(10) NOT NULL,
    prefix VARCHAR(20),
    suffix VARCHAR(20),
    allow_manual_entry BOOLEAN NOT NULL DEFAULT FALSE,
    print_metadata TEXT,
    next_number BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_custom_voucher_series
        UNIQUE (shop_code, voucher_type, series_code, financial_year),

    CONSTRAINT chk_series_next_number_positive
        CHECK (next_number > 0)
);

CREATE INDEX idx_custom_voucher_series_lookup
    ON custom_voucher_series(shop_code, voucher_type, financial_year);

CALL create_shop_isolation_policy(
    'custom_voucher_series',
    'shop_isolation_custom_voucher_series'
);

-- Reuse voucher table for series-scoped bills: raw voucher_number = bill_no.
-- Formatted prefix+number+suffix is computed on read.
ALTER TABLE voucher
    ADD COLUMN series_id BIGINT REFERENCES custom_voucher_series(id),
    ADD COLUMN financial_year VARCHAR(10);

CREATE INDEX idx_voucher_series_bill
    ON voucher(shop_code, series_id, financial_year, voucher_number);

-- Unique backstop so concurrent allocates can never duplicate a bill no.
-- NULL series_id rows (legacy payment/receipt/contra/journal) stay distinct in Postgres.
ALTER TABLE voucher
    ADD CONSTRAINT uk_voucher_series_bill
    UNIQUE (shop_code, series_id, financial_year, voucher_number);
