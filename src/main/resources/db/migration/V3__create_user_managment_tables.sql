-- ============================================
-- AUTHENTICATION & CREDENTIALS
-- ============================================

CREATE TABLE user_accounts (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    pass_hash VARCHAR(255) NOT NULL,
    uuid user_code NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_user_accounts_username ON user_accounts(username);
CREATE INDEX idx_user_accounts_uuid ON user_accounts(uuid);


-- ============================================
-- USERS (All Roles: SUPERADMIN, ADMIN, STAFF)
-- ============================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    uuid user_code NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    -- role VARCHAR(50) NOT NULL,
    category_id INT NULL,
    is_super_admin BOOLEAN DEFAULT FALSE,
    -- permissions BIGINT[] DEFAULT ARRAY[]::BIGINT[] NOT NULL,
    -- shop_code INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_category
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT;

CREATE INDEX idx_users_uuid ON users(uuid);
CREATE INDEX idx_users_is_superadmin ON users(is_super_admin);

-- RLS shop_code
-- By Pass RLS for super admin --> ShopCodeTransactionManager.class
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
CREATE POLICY shop_isolation ON users
    FOR ALL
    USING (-- for select, update and delete
        current_setting('app.bypass_rls', false)::BOOLEAN = true
        OR
        shop_code = current_setting('app.shop_code', false)::INTEGER
    )
    WITH CHECK (-- for insert and update
        current_setting('app.bypass_rls', false)::BOOLEAN = true
        OR
        shop_code = current_setting('app.shop_code', false)::INTEGER
    );