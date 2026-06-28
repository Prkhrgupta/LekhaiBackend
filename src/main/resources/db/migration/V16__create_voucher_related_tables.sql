-- Store generated voucher number metadata
CREATE TABLE voucher (
    id BIGSERIAL PRIMARY KEY,
    shop_code INTEGER NOT NULL
        DEFAULT current_setting('app.shop_code', false)::INTEGER,
    voucher_type VARCHAR(30) NOT NULL,
    voucher_number BIGINT NOT NULL,
    voucher_date DATE NOT NULL,
    narration TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_voucher_number
        UNIQUE (shop_code, voucher_type, voucher_number)
);

CREATE INDEX idx_voucher_date ON voucher(shop_code, voucher_date);

CREATE INDEX idx_voucher_type ON voucher(shop_code, voucher_type);

CALL create_shop_isolation_policy('voucher', 'shop_isolation_voucher');


-- Store actual transactions for particular voucher
CREATE TABLE voucher_entry (
    id BIGSERIAL PRIMARY KEY,
    shop_code INTEGER NOT NULL
        DEFAULT current_setting('app.shop_code', false)::INTEGER,
    voucher_id BIGINT NOT NULL,
    ledger_id BIGINT NOT NULL,
    line_number INTEGER NOT NULL,
    debit_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    credit_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_voucher FOREIGN KEY (voucher_id) REFERENCES voucher(id),

    CONSTRAINT fk_account FOREIGN KEY (ledger_id) REFERENCES ledger(id),

    CONSTRAINT uk_voucher_entry_line UNIQUE (voucher_id, line_number),

    -- Always follow double entry, No single row will have both Dr and Cr NON-ZERO
    CONSTRAINT chk_only_one_side
        CHECK (
            (debit_amount > 0 AND credit_amount = 0)
            OR
            (credit_amount > 0 AND debit_amount = 0)
        )
);

CREATE INDEX idx_voucher_entry_voucher ON voucher_entry(voucher_id);

CREATE INDEX idx_voucher_entry_account ON voucher_entry(shop_code, ledger_id);

CALL create_shop_isolation_policy('voucher_entry', 'shop_isolation_voucher_entry');


-- to maintain voucher type wise counter
CREATE TABLE voucher_counter (
    id BIGSERIAL PRIMARY KEY,
    shop_code INTEGER NOT NULL
        DEFAULT current_setting('app.shop_code', false)::INTEGER,
    voucher_type VARCHAR(30) NOT NULL,
    next_number BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_voucher_counter UNIQUE (shop_code, voucher_type),

    CONSTRAINT chk_next_number_positive
        CHECK (next_number > 0)

);

CREATE INDEX idx_voucher_counter_shop_type ON voucher_counter(shop_code, voucher_type);

CALL create_shop_isolation_policy(
    'voucher_counter',
    'shop_isolation_voucher_counter'
);