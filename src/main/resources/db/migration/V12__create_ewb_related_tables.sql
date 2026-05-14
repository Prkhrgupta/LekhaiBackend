CREATE TABLE gsp_user_credentials (
    id BIGSERIAL PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    shop_code INTEGER NOT NULL UNIQUE
);

CREATE TABLE ewb_records (
    id BIGSERIAL PRIMARY KEY,
    ewb_no BIGINT NOT NULL UNIQUE,
    eway_bill_date TIMESTAMP NOT NULL,
    valid_upto TIMESTAMP,
    status VARCHAR(10) NOT NULL,
    reject_status BOOLEAN DEFAULT FALSE,
    gen_mode VARCHAR(10),
    supply_type VARCHAR(5),
    sub_supply_type VARCHAR(10),
    document_type VARCHAR(10),
    document_number VARCHAR(50),
    document_date DATE,
    generator_gstin VARCHAR(15) NOT NULL,

    from_gstin VARCHAR(15),
    from_trade_name VARCHAR(255),
    from_address_line_1 TEXT,
    from_address_line_2 TEXT,
    from_place VARCHAR(255),
    from_pincode INTEGER,
    from_state_code CHAR(2),

    to_gstin VARCHAR(15),
    to_trade_name VARCHAR(255),
    to_address_line_1 TEXT,
    to_address_line_2 TEXT,
    to_place VARCHAR(255),
    to_pincode INTEGER,
    to_state_code CHAR(2),

    transporter_gstin VARCHAR(15),
    transporter_name VARCHAR(255),
    total_value NUMERIC(15,2),
    total_invoice_value NUMERIC(15,2),
    cgst_value NUMERIC(15,2),
    sgst_value NUMERIC(15,2),
    igst_value NUMERIC(15,2),
    cess_value NUMERIC(15,2),
    other_value NUMERIC(15,2),
    cess_non_advol_value NUMERIC(15,2),
    actual_distance INTEGER,
    valid_days INTEGER,

    extended_times INTEGER DEFAULT 0,

    vehicle_type VARCHAR(5),

    transaction_type INTEGER,

    is_delivered BOOLEAN DEFAULT FALSE,

    shop_code INTEGER DEFAULT current_setting('app.shop_code', false)::INTEGER,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CALL create_shop_isolation_policy('ewb_records', 'shop_isolation_ewb_records');

CREATE TABLE ewb_vehicle_details (
    id BIGSERIAL PRIMARY KEY,
    ewb_record_id BIGINT NOT NULL,
    update_mode VARCHAR(20),
    vehicle_number VARCHAR(20) NOT NULL,
    from_place VARCHAR(255),
    from_state_code CHAR(2),
    trip_sheet_number BIGINT,
    transporter_gstin VARCHAR(15),
    entered_date TIMESTAMP,
    transport_mode VARCHAR(10),
    transport_document_number VARCHAR(100),
    transport_document_date DATE,
    group_number VARCHAR(20),
    shop_code INTEGER DEFAULT current_setting('app.shop_code', false)::INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ewb_vehicle_ewb
        FOREIGN KEY (ewb_record_id)
        REFERENCES ewb_records(id)
        ON DELETE CASCADE
);
CALL create_shop_isolation_policy('ewb_vehicle_details', 'shop_isolation_ewb_vehicle_details');

-- INDEXES

CREATE INDEX idx_ewb_records_ewb_no
    ON ewb_records(ewb_no);

CREATE INDEX idx_ewb_records_document_number
    ON ewb_records(document_number);

CREATE INDEX idx_ewb_records_generator_gstin
    ON ewb_records(generator_gstin);

CREATE INDEX idx_ewb_records_valid_upto
    ON ewb_records(valid_upto);

CREATE INDEX idx_ewb_vehicle_ewb_record_id
    ON ewb_vehicle_details(ewb_record_id);

CREATE INDEX idx_ewb_vehicle_vehicle_number
    ON ewb_vehicle_details(vehicle_number);