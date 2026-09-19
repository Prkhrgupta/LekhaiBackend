-- TDS SECTION MASTER (STATIC, system-level like `state` — not shop-scoped, no RLS)
--
-- Nature-of-payment master a party ledger points at to say which TDS provision
-- applies to it. Statutory rates and thresholds change with every Finance Act,
-- so they are seeded here and every later change must be a NEW migration that
-- updates or deactivates rows (never edit this file).
--
-- ⚠ VERIFY BEFORE RELEASE: codes use the Income-tax Act, 1961 section numbers
-- still used on challans/returns; the Income-tax Act, 2025 (in force from
-- 1 Apr 2026) consolidates TDS into section 393. Rates/limits below reflect the
-- Finance Act 2025 position and must be re-checked against the current law.
-- A NULL limit means "no threshold of that kind".
CREATE TABLE tds_section (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(150) NOT NULL,
    rate_individual_huf NUMERIC(6, 3) NOT NULL,
    rate_others NUMERIC(6, 3) NOT NULL,
    rate_no_pan NUMERIC(6, 3) NOT NULL,
    single_transaction_limit NUMERIC(18, 2),
    monthly_limit NUMERIC(18, 2),
    annual_limit NUMERIC(18, 2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO tds_section
    (code, description, rate_individual_huf, rate_others, rate_no_pan,
     single_transaction_limit, monthly_limit, annual_limit)
VALUES
    ('194A',    'Interest other than interest on securities', 10.000, 10.000, 20.000, NULL,     NULL,     10000.00),
    ('194C',    'Payment to contractors / sub-contractors',    1.000,  2.000, 20.000, 30000.00, NULL,    100000.00),
    ('194H',    'Commission or brokerage',                     2.000,  2.000, 20.000, NULL,     NULL,     20000.00),
    ('194I(a)', 'Rent - plant, machinery or equipment',        2.000,  2.000, 20.000, NULL,     50000.00, NULL),
    ('194I(b)', 'Rent - land, building or furniture',         10.000, 10.000, 20.000, NULL,     50000.00, NULL),
    ('194J(a)', 'Fees for technical services',                 2.000,  2.000, 20.000, NULL,     NULL,     50000.00),
    ('194J(b)', 'Fees for professional services',             10.000, 10.000, 20.000, NULL,     NULL,     50000.00),
    ('194Q',    'Purchase of goods',                           0.100,  0.100,  5.000, NULL,     NULL,   5000000.00);
