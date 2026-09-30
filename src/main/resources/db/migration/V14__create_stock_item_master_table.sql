CREATE TABLE uom_master (
    id BIGSERIAL PRIMARY KEY,
    unit_name VARCHAR(255) NOT NULL,
    quantity_code VARCHAR(10) NOT NULL,

    -- Audit & Shop
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER
);

CREATE INDEX idx_uom_name ON uom_master(unit_name);

CALL create_shop_isolation_policy('uom_master', 'shop_isolation_uom_master');

CREATE TABLE stock_item_master (
    id BIGSERIAL PRIMARY KEY,

    finished_raw_material VARCHAR(20) CHECK (finished_raw_material IN ('FINISHED', 'RAW')),
    item_category_id BIGINT REFERENCES item_category_master(id),
    item_factory_id BIGINT REFERENCES item_factory_master(id),
    item_name VARCHAR(255) NOT NULL,
    purchase_price NUMERIC(14, 2) DEFAULT 0.00,
    sale_price NUMERIC(14, 2) DEFAULT 0.00,
    commodity_id BIGINT REFERENCES commodity_master(item_id),
    primary_uom_id BIGINT NOT NULL REFERENCES uom_master(id),
    alternate_uom_id BIGINT REFERENCES uom_master(id),
    conversion_factor NUMERIC(14, 4) CHECK (conversion_factor IS NULL OR conversion_factor > 0),

    CONSTRAINT chk_stock_item_alternate_factor
        CHECK ((alternate_uom_id IS NULL AND conversion_factor IS NULL)
            OR (alternate_uom_id IS NOT NULL AND conversion_factor IS NOT NULL)),
    CONSTRAINT chk_stock_item_alternate_distinct
        CHECK (alternate_uom_id IS NULL OR alternate_uom_id <> primary_uom_id),

    opening_qty NUMERIC(14, 3) DEFAULT 0.000,
    opening_rate NUMERIC(14, 4) DEFAULT 0.0000,
    opening_value NUMERIC(14, 2) DEFAULT 0.00,

    -- Audit & Shop
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER
);

CREATE INDEX idx_stock_item_name ON stock_item_master(item_name);
CREATE INDEX idx_stock_item_primary_uom ON stock_item_master(primary_uom_id);
CREATE INDEX idx_stock_item_alternate_uom ON stock_item_master(alternate_uom_id);

CALL create_shop_isolation_policy('stock_item_master', 'shop_isolation_stock_item_master');
