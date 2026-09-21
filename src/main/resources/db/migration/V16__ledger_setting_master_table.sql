-- ==============================================================================
-- 1. GENERAL LEDGER SETTINGS MASTER
-- ==============================================================================
CREATE TABLE general_ledger_setting_master (
   id BIGSERIAL PRIMARY KEY,

   freight_packing_ledger_id BIGINT REFERENCES ledger(id),
   round_off_ledger_id BIGINT REFERENCES ledger(id),
   tds_percentage NUMERIC(6, 3) DEFAULT 0.000,
   tds_ledger_id BIGINT REFERENCES ledger(id),
    tcs_percentage NUMERIC(6, 3) DEFAULT 0.000,
    tcs_ledger_id BIGINT REFERENCES ledger(id),
    output_cess_ledger_id BIGINT REFERENCES ledger(id),
    input_cess_ledger_id BIGINT REFERENCES ledger(id),

    -- Audit & Shop
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   is_deleted BOOLEAN DEFAULT FALSE,
   shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER,

   CONSTRAINT uq_general_ledger_setting_shop UNIQUE (shop_code)
);

CREATE INDEX idx_general_ledger_setting_shop ON general_ledger_setting_master(shop_code);

CALL create_shop_isolation_policy('general_ledger_setting_master', 'shop_isolation_general_ledger_setting_master');

-- ==============================================================================
-- 2. SALE LEDGER SETTING MASTER
-- ==============================================================================
CREATE TABLE sale_ledger_setting_master (
    id BIGSERIAL PRIMARY KEY,

    sale_ledger_id BIGINT NOT NULL REFERENCES ledger(id),
    sale_type VARCHAR(20),
    gst_rate NUMERIC(6, 3) DEFAULT 0.000,

    cgst_ledger_id BIGINT REFERENCES ledger(id),
    sgst_ledger_id BIGINT REFERENCES ledger(id),
    igst_ledger_id BIGINT REFERENCES ledger(id),

    -- Audit & Shop
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER,

    CONSTRAINT uq_sale_ledger_setting UNIQUE (shop_code, sale_type, gst_rate)
);

CREATE INDEX idx_sale_ledger_setting_lookup ON sale_ledger_setting_master(shop_code, sale_type, gst_rate);
CREATE INDEX idx_sale_ledger_setting_ledger ON sale_ledger_setting_master(sale_ledger_id);

CALL create_shop_isolation_policy('sale_ledger_setting_master', 'shop_isolation_sale_ledger_setting_master');

-- ==============================================================================
-- 3. PURCHASE LEDGER SETTING MASTER
-- ==============================================================================
CREATE TABLE purchase_ledger_setting_master (
    id BIGSERIAL PRIMARY KEY,

    purchase_ledger_id BIGINT NOT NULL REFERENCES ledger(id),
    purchase_type VARCHAR(20),
    gst_rate NUMERIC(6, 3) DEFAULT 0.000,

    cgst_ledger_id BIGINT REFERENCES ledger(id),
    sgst_ledger_id BIGINT REFERENCES ledger(id),
    igst_ledger_id BIGINT REFERENCES ledger(id),

    -- Audit & Shop
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER,

    CONSTRAINT uq_purchase_ledger_setting UNIQUE (shop_code, purchase_type, gst_rate)
);

CREATE INDEX idx_purchase_ledger_setting_lookup ON purchase_ledger_setting_master(shop_code, purchase_type, gst_rate);
CREATE INDEX idx_purchase_ledger_setting_ledger ON purchase_ledger_setting_master(purchase_ledger_id);

CALL create_shop_isolation_policy('purchase_ledger_setting_master', 'shop_isolation_purchase_ledger_setting_master');