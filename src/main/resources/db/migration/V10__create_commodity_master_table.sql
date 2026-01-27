CREATE TABLE commodity_master (
    item_id BIGSERIAL PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL,
    hsn_sac_code VARCHAR(50),
    description VARCHAR(500),
    uom VARCHAR(50),
    
    gst_rate_sale NUMERIC(10, 2) DEFAULT 0.00,
    gst_rate_purchase NUMERIC(10, 2) DEFAULT 0.00,
    
    is_sale_purchase_active BOOLEAN DEFAULT FALSE,

    -- Sale Ledger Config
    sale_ac_in_state_id BIGINT REFERENCES ledger(id),
    sale_cgst_percent NUMERIC(5, 2) DEFAULT 0.00,
    sale_sgst_percent NUMERIC(5, 2) DEFAULT 0.00,
    sale_cess_percent NUMERIC(5, 2) DEFAULT 0.00,
    
    round_off_ac_id BIGINT REFERENCES ledger(id),
    
    sale_ac_out_state_id BIGINT REFERENCES ledger(id),
    sale_igst_percent NUMERIC(5, 2) DEFAULT 0.00,
    sale_cess_out_percent NUMERIC(5, 2) DEFAULT 0.00,

    -- Purchase Ledger Config
    purchase_ac_in_state_id BIGINT REFERENCES ledger(id),
    purchase_cgst_percent NUMERIC(5, 2) DEFAULT 0.00,
    purchase_sgst_percent NUMERIC(5, 2) DEFAULT 0.00,
    purchase_cess_percent NUMERIC(5, 2) DEFAULT 0.00,
    
    purchase_ac_out_state_id BIGINT REFERENCES ledger(id),
    purchase_igst_percent NUMERIC(5, 2) DEFAULT 0.00,
    purchase_cess_out_percent NUMERIC(5, 2) DEFAULT 0.00,

    -- Audit & Shop
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER
);

CREATE INDEX idx_item_name ON commodity_master(item_name);
CREATE INDEX idx_hsn_code ON commodity_master(hsn_sac_code);

CALL create_shop_isolation_policy('commodity_master', 'shop_isolation_commodity_master');
