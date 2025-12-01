CREATE TABLE features (
    id SERIAL PRIMARY KEY,
    feature_key VARCHAR(100) UNIQUE NOT NULL,
    parent_id INTEGER REFERENCES features(id) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    icon VARCHAR(100) NOT NULL,
    route VARCHAR(255),
    bit_position INTEGER UNIQUE,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true
);

-- Indexes
CREATE INDEX idx_features_parent_id ON features(parent_id);
CREATE INDEX idx_features_bit_position ON features(bit_position) WHERE bit_position IS NOT NULL;