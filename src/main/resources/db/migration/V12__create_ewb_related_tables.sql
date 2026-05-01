CREATE TABLE gsp_user_credentials (
    id BIGSERIAL PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    shop_code INTEGER NOT NULL UNIQUE
);

CREATE TABLE ewb_records (
    id BIGSERIAL PRIMARY KEY,
    ewb_no VARCHAR(15) NOT NULL UNIQUE,
    ewb_date TIMESTAMP NOT NULL,
    status VARCHAR(10) NOT NULL,
    generator_gstin VARCHAR(15) NOT NULL,
    doc_no VARCHAR(50),
    doc_date DATE,
    delivery_pin_code INTEGER,
    delivery_state_code CHAR(2),
    delivery_place VARCHAR(100),
    valid_up_to TIMESTAMP,
    extended_times INTEGER DEFAULT 0,
    reject_status BOOLEAN,
    is_delivered BOOLEAN DEFAULT FALSE,
    shop_code INTEGER DEFAULT current_setting('app.shop_code', false)::INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE ewb_records
    ADD CONSTRAINT fk_state_code
    FOREIGN KEY (delivery_state_code) REFERENCES state(state_code) ON DELETE RESTRICT;

CALL create_shop_isolation_policy('ewb_records', 'shop_isolation_ewb_records');
