CREATE TABLE item_category_master (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,

    -- Audit & Shop
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER
);

CREATE INDEX idx_item_category_name ON item_category_master(name);

CALL create_shop_isolation_policy('item_category_master', 'shop_isolation_item_category_master');
