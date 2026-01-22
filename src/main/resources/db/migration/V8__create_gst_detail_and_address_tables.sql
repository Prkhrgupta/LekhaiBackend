
-- ===============================
-- ACCOUNT ADDRESS
-- ===============================
CREATE TABLE account_address (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,

    address_line1 TEXT NOT NULL,
    address_line2 TEXT,
    address_line3 TEXT,
    city VARCHAR(50),
    state_id BIGINT,
    pincode CHAR(6),

    is_primary BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_address_account
        FOREIGN KEY (account_id)
        REFERENCES account_master(id),

    CONSTRAINT fk_address_state
        FOREIGN KEY (state_id)
        REFERENCES state_master(id)
);


CREATE TABLE gst_details (
    id BIGSERIAL PRIMARY KEY,

    account_id BIGINT NOT NULL,

    registration_type VARCHAR(20) NOT NULL,
    -- Example values:
    -- CUSTOMER, SUPPLIER, BOTH

    is_ecommerce_operator BOOLEAN NOT NULL DEFAULT FALSE,

    gstin_or_uin VARCHAR(15) NOT NULL,

    party_type VARCHAR(30) NOT NULL,
    -- Example values:
    -- REGULAR, SEZ, DEEMED_EXPORT, GOVERNMENT_ENTITY

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_gst_account
        FOREIGN KEY (account_id)
        REFERENCES account_master(id),

    CONSTRAINT uq_gstin UNIQUE (gstin_or_uin)
);

