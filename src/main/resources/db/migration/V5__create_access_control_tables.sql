-- ============================================
-- ROLE-CATEGORY PERMISSIONS
-- ============================================

CREATE TABLE role_permissions (
    id SERIAL PRIMARY KEY,
    role VARCHAR(50) NOT NULL,
    category_id INT NOT NULL,
    permissions BIGINT[] DEFAULT ARRAY[]::BIGINT[] NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_role_category UNIQUE(role, category_id)
);

ALTER TABLE role_permissions
    ADD CONSTRAINT fk_role_permissions_category
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE;

CREATE INDEX idx_role_permissions_role ON role_permissions(role);
CREATE INDEX idx_role_permissions_category_id ON role_permissions(category_id);


-- ============================================
-- USER-SHOP ACCESS MAPPING
-- ============================================

CREATE TABLE user_shop_access (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    shop_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_selected_default BOOLEAN, -- can be NULL, if NULL show shop select menu
    permissions BIGINT[] DEFAULT ARRAY[]::BIGINT[] NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_shop_access UNIQUE(user_id, shop_id)
);

ALTER TABLE user_shop_access
    ADD CONSTRAINT fk_user_shop_access_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_shop_access
    ADD CONSTRAINT fk_user_shop_access_shop
    FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE;

CREATE INDEX idx_user_shop_access_user_id ON user_shop_access(user_id);
CREATE INDEX idx_user_shop_access_shop_id ON user_shop_access(shop_id);