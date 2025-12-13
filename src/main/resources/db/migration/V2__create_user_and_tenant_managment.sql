-- ============================================
-- USER DETAILS
-- ============================================

CREATE TABLE user_details (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    category_id INT NOT NULL,
    permission_bit BIGINT[] DEFAULT ARRAY[]::BIGINT[] NOT NULL,
    special_feature_bits BIGINT[] DEFAULT ARRAY[]::BIGINT[] NOT NULL,
    tenant VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

ALTER TABLE user_details
    ADD CONSTRAINT fk_user_details_category
    FOREIGN KEY (category_id) REFERENCES category_master(id) ON DELETE RESTRICT;

CREATE INDEX idx_user_details_uuid ON user_details(uuid);
CREATE INDEX idx_user_details_tenant ON user_details(tenant);
CREATE INDEX idx_user_details_tenant_role ON user_details(tenant, role);


-- ============================================
-- TENANT DETAILS
-- ============================================

CREATE TABLE tenant_details (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL, -- will almost always be ADMIN ( should we need this explicitly)
    category_id INT NOT NULL,
    permission_bit BIGINT[] DEFAULT ARRAY[]::BIGINT[] NOT NULL,
    gst_in VARCHAR(15) NULL UNIQUE,
    firm_name VARCHAR(255) NOT NULL,
    registered_address TEXT NULL,
    tenant INTEGER NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

ALTER TABLE tenant_details
    ADD CONSTRAINT fk_tenant_details_category
    FOREIGN KEY (category_id) REFERENCES category_master(id) ON DELETE RESTRICT;

CREATE INDEX idx_tenant_details_uuid ON tenant_details(uuid);
CREATE INDEX idx_tenant_details_tenant ON tenant_details(tenant);