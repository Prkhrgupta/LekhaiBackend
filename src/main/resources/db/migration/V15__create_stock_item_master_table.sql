CREATE TABLE stock_item_master (
    id BIGSERIAL PRIMARY KEY,

    finished_raw_material VARCHAR(20) CHECK (finished_raw_material IN ('FINISHED', 'RAW')),
    item_category_id BIGINT REFERENCES item_category_master(id),
    item_factory_id BIGINT REFERENCES item_factory_master(id),
    item_name VARCHAR(255) NOT NULL,
    purchase_price NUMERIC(14, 2) DEFAULT 0.00,
    sale_price NUMERIC(14, 2) DEFAULT 0.00,
    commodity_id BIGINT REFERENCES commodity_master(item_id),
    rate_per VARCHAR(10) CHECK (rate_per IN ('PCS', 'METER')),

    opening_pcs NUMERIC(14, 2) DEFAULT 0.00,
    opening_meter NUMERIC(14, 3) DEFAULT 0.000,
    opening_rate NUMERIC(14, 4) DEFAULT 0.0000,
    opening_value NUMERIC(14, 2) DEFAULT 0.00,

    -- Audit & Shop
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER
);

CREATE INDEX idx_stock_item_name ON stock_item_master(item_name);

CALL create_shop_isolation_policy('stock_item_master', 'shop_isolation_stock_item_master');
