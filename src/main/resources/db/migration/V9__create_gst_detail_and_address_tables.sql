-- ===============================
-- ACCOUNT ADDRESS
-- ===============================
CREATE TABLE ledger_address (
    id BIGSERIAL PRIMARY KEY,
    ledger_id BIGINT NOT NULL REFERENCES ledger(id),
    address_line1 TEXT,
    address_line2 TEXT,
    address_line3 TEXT,
    city VARCHAR(50),
    state_id CHAR(2) REFERENCES state(state_code),
    area_id BIGINT REFERENCES area(id),
    pincode CHAR(6),
    distance NUMERIC(8,2),
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER
);

CALL create_shop_isolation_policy('ledger_address', 'shop_isolation_ledger_address');

CREATE TABLE gst_details (
    id BIGSERIAL PRIMARY KEY,
    ledger_id BIGINT NOT NULL REFERENCES ledger(id),
    registration_type VARCHAR(20) NOT NULL,
    is_ecommerce_operator BOOLEAN NOT NULL DEFAULT FALSE,
    gstin_or_uin VARCHAR(15) NOT NULL,
    party_type VARCHAR(30) NOT NULL,
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_gst_in_shop UNIQUE (shop_code, gstin_or_uin)
);

CALL create_shop_isolation_policy('gst_details', 'shop_isolation_gst_details');
