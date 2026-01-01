-- ============================================
--  SUPERADMIN MASTER
-- ============================================

CREATE TABLE super_admin_master (
    id BIGSERIAL PRIMARY KEY,
    uuid user_code NOT NULL UNIQUE,
    name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);


-- ============================================
-- TENANT DETAILS
-- ============================================

CREATE TABLE tenant_details (
    id BIGSERIAL PRIMARY KEY,
    uuid user_code UNIQUE,
    category_id INT NOT NULL,
    is_default BOOLEAN,
    gst_in VARCHAR(15) NULL UNIQUE,
    firm_name VARCHAR(255) NOT NULL,
    registered_address TEXT NULL,
    tenant INTEGER NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- TODO : add a combined unique constraint on uuid + tenant

ALTER TABLE tenant_details
    ADD CONSTRAINT fk_tenant_details_category
    FOREIGN KEY (category_id) REFERENCES category_master(id) ON DELETE RESTRICT;

CREATE INDEX idx_tenant_details_uuid ON tenant_details(uuid);
CREATE INDEX idx_tenant_details_tenant ON tenant_details(tenant);


-- ============================================
-- ADMIN DETAILS
-- ============================================

CREATE TABLE admin_details (
    id BIGSERIAL PRIMARY KEY,
    uuid user_code NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    category_id BIGINT NOT NULL,
    permission_bit BIGINT[] DEFAULT ARRAY[]::BIGINT[] NOT NULL,
    tenant INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- USER INFORMATION
-- ============================================

CREATE TABLE user_information (
    id BIGSERIAL PRIMARY KEY,
    uuid user_code NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    category_id BIGINT NOT NULL,
    permission_bit BIGINT[] DEFAULT ARRAY[]::BIGINT[] NOT NULL,
    tenant INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

ALTER TABLE user_information
    ADD CONSTRAINT fk_user_information_category
    FOREIGN KEY (category_id) REFERENCES category_master(id) ON DELETE RESTRICT;

CREATE INDEX idx_user_information_uuid ON user_information(uuid);
CREATE INDEX idx_user_information_tenant ON user_information(tenant);
CREATE INDEX idx_user_information_tenant_role ON user_information(tenant, role);