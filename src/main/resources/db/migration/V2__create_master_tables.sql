-- ============================================
-- CATEGORIES (Business types) Eg: CLOTH, GOLD, SILVER etc.
-- ============================================

CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    permissions BIGINT[] DEFAULT ARRAY[]::BIGINT[] NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ============================================
-- FEATURES/PERMISSIONS
-- ============================================

CREATE TABLE features (
    id SERIAL PRIMARY KEY,
    feature_key VARCHAR(100) NOT NULL,
    parent_id INTEGER REFERENCES features(id) ON DELETE CASCADE, -- Need cascade since, logic for fetchFeatures will break
    title VARCHAR(100) NOT NULL,
    icon VARCHAR(100),
    bit_position INTEGER UNIQUE,
    route VARCHAR(150) UNIQUE,
    display_order INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_features_parent_id ON features(parent_id);
CREATE INDEX idx_features_bit_position ON features(bit_position) WHERE bit_position IS NOT NULL;