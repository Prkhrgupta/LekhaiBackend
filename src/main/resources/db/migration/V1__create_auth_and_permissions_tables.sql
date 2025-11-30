-- ============================================
-- AUTHENTICATION & CREDENTIALS
-- ============================================

CREATE TABLE user_credentials (
    id BIGSERIAL PRIMARY KEY,
    pass_hash VARCHAR(255) NOT NULL,
    uuid VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_login TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
);

CREATE INDEX idx_user_credentials_uuid ON user_credentials(uuid);
CREATE INDEX idx_user_credentials_active ON user_credentials(is_active) WHERE is_active = TRUE;


-- ============================================
-- CATEGORY MASTER
-- ============================================

CREATE TABLE category_master (
    id SERIAL PRIMARY KEY,
    category VARCHAR(100) NOT NULL UNIQUE,
    permission BIGINT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_category_master_category ON category_master(category);


-- ============================================
-- ROLE CATEGORY MASTER
-- ============================================

CREATE TABLE role_category_master (
    id SERIAL PRIMARY KEY,
    role VARCHAR(50) NOT NULL,
    category_id INT NOT NULL,
    permission BIGINT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_role_category UNIQUE (role, category_id)
);

ALTER TABLE role_category_master
    ADD CONSTRAINT fk_role_category_master_category
    FOREIGN KEY (category_id) REFERENCES category_master(id) ON DELETE CASCADE;

CREATE INDEX idx_role_category_master_role ON role_category_master(role);
CREATE INDEX idx_role_category_master_category ON role_category_master(category_id);