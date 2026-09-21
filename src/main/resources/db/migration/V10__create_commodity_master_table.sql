CREATE TABLE commodity_master (
    item_id BIGSERIAL PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL,
    hsn_sac_code VARCHAR(50),
    description VARCHAR(500),
    unit_of_measure VARCHAR(50),

    -- Single GST rate dictating both purchase and sale tax
    gst_rate NUMERIC(5, 2),

    -- Cess rate for the commodity; ledgers live in general_ledger_setting_master
    cess_percentage NUMERIC(6, 3) DEFAULT 0.000,
    
    -- Audit & Shop
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER
);

CREATE INDEX idx_item_name ON commodity_master(item_name);
CREATE INDEX idx_hsn_code ON commodity_master(hsn_sac_code);

CALL create_shop_isolation_policy('commodity_master', 'shop_isolation_commodity_master');
