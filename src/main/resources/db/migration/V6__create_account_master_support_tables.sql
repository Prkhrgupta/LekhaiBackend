-- ===============================
-- STATE MASTER (STATIC)
-- ===============================
CREATE TABLE state_master (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    gst_code CHAR(2) NOT NULL UNIQUE
    type varchar(255) NOT NULL,
    vehicle_code CHAR(2) NOT NULL UNIQUE -- can be used as a PK
);

-- ===============================
-- AREA MASTER
-- ===============================
CREATE TABLE area_master (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    state_id BIGINT NOT NULL,

    CONSTRAINT fk_area_state
        FOREIGN KEY (state_id)
        REFERENCES state_master(id)
);

-- ===============================
-- BROKER MASTER
-- ===============================
CREATE TABLE broker_master (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(15)
);

-- ===============================
-- TRANSPORT MASTER
-- ===============================
CREATE TABLE transport_master (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(15),
    gst_no VARCHAR(15)
);

CREATE TABLE account_group (
   id BIGSERIAL PRIMARY KEY,

    name VARCHAR(100) NOT NULL,

    parent_id BIGINT NULL,

    nature VARCHAR(20) NOT NULL CHECK (
        nature IN ('ASSET', 'LIABILITY', 'INCOME', 'EXPENSE')
    ),

    affects_gross_profit BOOLEAN NOT NULL DEFAULT FALSE,

    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
--    is_system BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_account_group_parent
        FOREIGN KEY (parent_id)
        REFERENCES account_group(id),

    CONSTRAINT uq_account_group UNIQUE (name, parent_id)
);

