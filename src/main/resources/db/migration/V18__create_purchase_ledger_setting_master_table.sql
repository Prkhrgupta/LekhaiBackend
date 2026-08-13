CREATE TABLE purchase_ledger_setting_master (
    id BIGSERIAL PRIMARY KEY,

    purchase_ledger_id BIGINT NOT NULL REFERENCES ledger(id),
    purchase_type VARCHAR(20) CHECK (purchase_type IN ('IN_STATE', 'OUT_STATE', 'EXPORT')),
    gst_rate NUMERIC(6, 3) DEFAULT 0.000,

    cgst_percentage NUMERIC(6, 3) DEFAULT 0.000,
    cgst_ledger_id BIGINT REFERENCES ledger(id),
    sgst_percentage NUMERIC(6, 3) DEFAULT 0.000,
    sgst_ledger_id BIGINT REFERENCES ledger(id),
    igst_percentage NUMERIC(6, 3) DEFAULT 0.000,
    igst_ledger_id BIGINT REFERENCES ledger(id),
    cess_percentage NUMERIC(6, 3) DEFAULT 0.000,
    cess_ledger_id BIGINT REFERENCES ledger(id),

    freight_packing_ledger_id BIGINT REFERENCES ledger(id),
    round_off_ledger_id BIGINT REFERENCES ledger(id),
    tds_percentage NUMERIC(6, 3) DEFAULT 0.000,
    tds_ledger_id BIGINT REFERENCES ledger(id),

    -- Audit & Shop
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER
);

CREATE INDEX idx_purchase_ledger_setting_ledger ON purchase_ledger_setting_master(purchase_ledger_id);

CALL create_shop_isolation_policy('purchase_ledger_setting_master', 'shop_isolation_purchase_ledger_setting_master');
