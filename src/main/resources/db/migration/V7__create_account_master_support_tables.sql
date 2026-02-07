-- ===============================
-- STATE MASTER (STATIC)
-- ===============================
CREATE TABLE state (
    state_code CHAR(2) PRIMARY KEY,
    state_name VARCHAR(50) NOT NULL,
    gst_code CHAR(2) NOT NULL UNIQUE,
    type varchar(255) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ===============================
-- AREA MASTER
-- ===============================
CREATE TABLE area (
    id BIGSERIAL PRIMARY KEY,
    area_name VARCHAR(100) NOT NULL,
    state_code CHAR(2),
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_area_state
        FOREIGN KEY (state_code)
        REFERENCES state(state_code)
);

CALL create_shop_isolation_policy('area', 'shop_isolation_area');

-- ===============================
-- BROKER MASTER
-- ===============================
CREATE TABLE broker (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(15),
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CALL create_shop_isolation_policy('broker', 'shop_isolation_broker');

-- ===============================
-- TRANSPORT MASTER
-- ===============================
CREATE TABLE transport (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(15),
    gst_no VARCHAR(15),
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CALL create_shop_isolation_policy('transport', 'shop_isolation_transport');

CREATE TABLE account_group (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT NULL,
    nature VARCHAR(20) NOT NULL CHECK (
        nature IN ('ASSET', 'LIABILITY', 'INCOME', 'EXPENSE')
    ),
    behaviour CHAR(2) NOT NULL CHECK (
        behaviour IN ('CR', 'DR')
    ),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_account_group_parent
        FOREIGN KEY (parent_id)
        REFERENCES account_group(id),

    CONSTRAINT uq_account_group UNIQUE (shop_code, name, parent_id)
);

INSERT INTO account_group (name, parent_id, nature, behaviour, is_primary, shop_code) VALUES
('Capital Account', NULL, 'LIABILITY', 'CR', true, 0),
('Loans (Liability)', NULL, 'LIABILITY', 'CR', true, 0),
('Current Liabilities', NULL, 'LIABILITY', 'CR', true, 0),
('Fixed Assets', NULL, 'ASSET', 'DR', true, 0),
('Investments', NULL, 'ASSET', 'DR', true, 0),
('Current Assets', NULL, 'ASSET', 'DR', true, 0),
('Branch / Divisions', NULL, 'LIABILITY', 'CR', true, 0),
('Suspense A/c', NULL, 'LIABILITY', 'CR', true, 0),
('Sales Accounts', NULL, 'INCOME', 'CR', true, 0),
('Purchase Accounts', NULL, 'EXPENSE', 'DR', true, 0),
('Direct Incomes', NULL, 'INCOME', 'CR', true, 0),
('Indirect Incomes', NULL, 'INCOME', 'CR', true, 0),
('Direct Expenses', NULL, 'EXPENSE', 'DR', true, 0),
('Indirect Expenses', NULL, 'EXPENSE', 'DR', true, 0),
('Misc. Expenses (ASSET)', NULL, 'ASSET', 'DR', true, 0);

CALL create_shop_isolation_policy('account_group', 'shop_isolation_account_group');