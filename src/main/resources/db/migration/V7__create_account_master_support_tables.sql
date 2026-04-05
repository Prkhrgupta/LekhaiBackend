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
    shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER,
    sitswift_code INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    sitswift_code INTEGER,
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
    sitswift_code INTEGER,
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

    sitswift_code INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_account_group_parent
        FOREIGN KEY (parent_id)
        REFERENCES account_group(id),

    CONSTRAINT uq_account_group UNIQUE (shop_code, name, parent_id)
);

CALL create_shop_isolation_policy('account_group', 'shop_isolation_account_group');