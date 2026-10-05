-- ============================================
-- SYSTEM LICENSING TABLE
-- ============================================

CREATE TABLE system_license (
    id BIGSERIAL PRIMARY KEY,
    license_id VARCHAR(64) NOT NULL UNIQUE,
    licensee_name VARCHAR(255) NOT NULL,
    admin_username VARCHAR(100) NOT NULL,
    raw_payload TEXT NOT NULL,
    signature TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_system_license_is_active ON system_license(is_active);
CREATE INDEX idx_system_license_admin_username ON system_license(admin_username);
