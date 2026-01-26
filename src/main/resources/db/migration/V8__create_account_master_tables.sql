-- ===============================
-- ACCOUNT MASTER (LEDGER)
-- ===============================
CREATE TABLE ledger (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    legal_name VARCHAR(150),
    account_group_id BIGINT NOT NULL REFERENCES account_group(id),
    opening_balance NUMERIC(14,2) NOT NULL DEFAULT 0,
    opening_balance_type CHAR(2) NOT NULL CHECK (
        opening_balance_type IN ('DR', 'CR')
    ),
    credit_limit NUMERIC(14,2),
    default_area_id BIGINT REFERENCES area(id),
    default_broker_id BIGINT REFERENCES broker(id),
    default_transport_id BIGINT REFERENCES transport(id),
    pan CHAR(10),
    aadhaar CHAR(12),
    tan CHAR(10),
    email VARCHAR(100),
    msme VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_account_pan_shop UNIQUE (shop_code, pan),
    CONSTRAINT uq_account_aadhaar_shop UNIQUE (shop_code, aadhaar),
    CONSTRAINT uq_account_tan_shop UNIQUE (shop_code, tan)
);

CALL create_shop_isolation_policy('ledger', 'shop_isolation_ledger');
