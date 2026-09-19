-- TDS details of a party ledger: whether tax is deducted on payments/credits to
-- it, under which section, the deductee's status, and any lower/nil deduction
-- certificate (section 197) it has furnished.
ALTER TABLE ledger
    ADD COLUMN is_tds_applicable BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN tds_section_id BIGINT REFERENCES tds_section(id),
    ADD COLUMN deductee_type VARCHAR(20)
        CHECK (deductee_type IN ('INDIVIDUAL_HUF', 'OTHERS')),
    ADD COLUMN ldc_certificate_number VARCHAR(20),
    ADD COLUMN ldc_rate NUMERIC(6, 3),
    ADD COLUMN ldc_valid_from DATE,
    ADD COLUMN ldc_valid_to DATE;
