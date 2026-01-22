-- ===============================
-- ACCOUNT MASTER (LEDGER)
-- ===============================
CREATE TABLE account_master (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(150) NOT NULL,
    legal_name VARCHAR(150),

    account_group_id BIGINT NOT NULL,

    opening_balance NUMERIC(14,2) NOT NULL DEFAULT 0,
    opening_balance_type CHAR(2) NOT NULL CHECK (
        opening_balance_type IN ('DR', 'CR')
    ),

    credit_limit NUMERIC(14,2),

    state_id BIGINT,

    default_area_id BIGINT,
    default_broker_id BIGINT,
    default_transport_id BIGINT,
    -- can be removed from here and use a is_default field in their master
    --- this looks good to me

    pan_no CHAR(10) UNIQUE, -- shopCode -RLS Insert, + shopCode (UNIQUE) TEST this
    aadhar_no CHAR(12) UNIQUE,
    tan_no CHAR(10) UNIQUE,

    email VARCHAR(100),
--    distance NUMERIC(8,2),
    msme_no VARCHAR(20),

    is_bill_wise BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_account_group
        FOREIGN KEY (group_id)
        REFERENCES account_group(id),

    CONSTRAINT fk_account_state
        FOREIGN KEY (state_id)
        REFERENCES state_master(id),

    CONSTRAINT fk_account_area
        FOREIGN KEY (area_id)
        REFERENCES area_master(id),

    CONSTRAINT fk_account_broker
        FOREIGN KEY (broker_id)
        REFERENCES broker_master(id),

    CONSTRAINT fk_account_transport
        FOREIGN KEY (transport_id)
        REFERENCES transport_master(id),

    CONSTRAINT uq_account_name UNIQUE (name)
);
