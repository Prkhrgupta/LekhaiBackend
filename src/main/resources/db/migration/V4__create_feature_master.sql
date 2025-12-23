CREATE TABLE features (
    id SERIAL PRIMARY KEY,
    feature_key VARCHAR(100) NOT NULL,
    parent_id INTEGER REFERENCES features(id) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    icon VARCHAR(100) NOT NULL,
    bit_position INTEGER UNIQUE,
    route VARCHAR(150) UNIQUE,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    is_deleted BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_features_parent_id ON features(parent_id);
CREATE INDEX idx_features_bit_position ON features(bit_position) WHERE bit_position IS NOT NULL;