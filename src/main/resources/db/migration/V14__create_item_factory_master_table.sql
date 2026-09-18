CREATE TABLE item_factory_master (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    percentage NUMERIC(5, 2) DEFAULT 0.00,

    -- Audit & Shop
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER
);

CREATE INDEX idx_item_factory_name ON item_factory_master(name);

CALL create_shop_isolation_policy('item_factory_master', 'shop_isolation_item_factory_master');
