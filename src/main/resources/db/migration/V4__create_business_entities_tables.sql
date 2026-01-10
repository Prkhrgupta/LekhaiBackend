-- ============================================
-- SHOPS (Business Entities / Tenants)
-- ============================================

CREATE TABLE shops (
    id BIGSERIAL PRIMARY KEY,
    shop_code INTEGER NOT NULL UNIQUE,
    firm_name VARCHAR(255) NOT NULL,
    category_id INT NOT NULL,
    gst_number VARCHAR(15) NULL UNIQUE,
    registered_address TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

ALTER TABLE shops
    ADD CONSTRAINT fk_shops_category
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT;

CREATE INDEX idx_shops_shop_code ON shops(shop_code);
CREATE INDEX idx_shops_category_id ON shops(category_id);