-- Seller state on the shop; compared against place of supply (via state.gst_code) for intra/inter-state GST.
ALTER TABLE shops ADD COLUMN state_code CHAR(2) REFERENCES state(state_code);

UPDATE shops s
SET state_code = st.state_code
FROM state st
WHERE s.gst_number IS NOT NULL
  AND st.gst_code = LEFT(s.gst_number, 2);

CREATE TABLE sales_invoice (
    id BIGSERIAL PRIMARY KEY,
    shop_code INTEGER NOT NULL
        DEFAULT current_setting('app.shop_code', false)::INTEGER,
    voucher_id BIGINT NOT NULL UNIQUE REFERENCES voucher(id),
    series_id BIGINT NOT NULL REFERENCES custom_voucher_series(id),
    series_code_snapshot VARCHAR(30) NOT NULL,

    bill_no BIGINT NOT NULL,
    bill_display VARCHAR(16) NOT NULL,
    financial_year VARCHAR(10) NOT NULL,
    bill_date DATE NOT NULL,

    party_ledger_id BIGINT NOT NULL REFERENCES ledger(id),
    party_name_snapshot VARCHAR(150) NOT NULL,
    party_gstin_snapshot VARCHAR(15),
    party_state_code_snapshot VARCHAR(2),
    billing_address_snapshot TEXT,
    shipping_address_snapshot TEXT,
    ship_to_id BIGINT,
    place_of_supply VARCHAR(2) NOT NULL,
    place_of_supply_source VARCHAR(10) NOT NULL,
    is_inter_state BOOLEAN NOT NULL DEFAULT FALSE,

    supply_type VARCHAR(20) NOT NULL DEFAULT 'B2B',
    is_rcm BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    total_goods_value NUMERIC(18,2) NOT NULL DEFAULT 0,
    total_discount NUMERIC(18,2) NOT NULL DEFAULT 0,
    -- TODO: add charges are deferred; always 0 in v1.
    total_add_charges NUMERIC(18,2) NOT NULL DEFAULT 0,
    total_taxable NUMERIC(18,2) NOT NULL DEFAULT 0,

    cgst_amt NUMERIC(18,2) NOT NULL DEFAULT 0,
    sgst_amt NUMERIC(18,2) NOT NULL DEFAULT 0,
    igst_amt NUMERIC(18,2) NOT NULL DEFAULT 0,
    cess_amt NUMERIC(18,2) NOT NULL DEFAULT 0,

    round_off NUMERIC(18,2) NOT NULL DEFAULT 0,
    net_amount NUMERIC(18,2) NOT NULL DEFAULT 0,

    metadata JSONB,
    remarks TEXT,
    cancelled_at TIMESTAMPTZ,
    cancel_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_sales_invoice_bill
    ON sales_invoice(shop_code, series_id, financial_year, bill_no);
CREATE INDEX idx_sales_invoice_date ON sales_invoice(shop_code, bill_date);
CREATE INDEX idx_sales_invoice_party ON sales_invoice(shop_code, party_ledger_id);

CALL create_shop_isolation_policy('sales_invoice', 'shop_isolation_sales_invoice');

CREATE TABLE sales_invoice_item (
    id BIGSERIAL PRIMARY KEY,
    shop_code INTEGER NOT NULL
        DEFAULT current_setting('app.shop_code', false)::INTEGER,
    sales_invoice_id BIGINT NOT NULL REFERENCES sales_invoice(id),
    srno INTEGER NOT NULL,

    stock_item_id BIGINT NOT NULL REFERENCES stock_item_master(id),
    item_name_snapshot VARCHAR(255) NOT NULL,
    hsn_code_snapshot VARCHAR(10) NOT NULL,
    category_name_snapshot VARCHAR(255),
    -- No godown master yet; plain nullable column, unused in v1.
    godown_id BIGINT,
    sale_ledger_id BIGINT NOT NULL REFERENCES ledger(id),

    quantity NUMERIC(18,3) NOT NULL DEFAULT 0,
    uom_id BIGINT NOT NULL,
    uom_snapshot VARCHAR(10) NOT NULL,
    alternate_quantity NUMERIC(18,3) NOT NULL DEFAULT 0,
    alternate_uom_snapshot VARCHAR(10),

    rate NUMERIC(18,2) NOT NULL,
    gross_amount NUMERIC(18,2) NOT NULL,
    discount_pct NUMERIC(18,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    taxable_value NUMERIC(18,2) NOT NULL,
    gst_rate_snapshot NUMERIC(18,2) NOT NULL,
    cess_rate_snapshot NUMERIC(18,2) NOT NULL DEFAULT 0,
    cess_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    remarks TEXT,

    CONSTRAINT uk_sales_invoice_item_line UNIQUE (sales_invoice_id, srno)
);

CREATE INDEX idx_sales_invoice_item_invoice ON sales_invoice_item(sales_invoice_id);

CALL create_shop_isolation_policy('sales_invoice_item', 'shop_isolation_sales_invoice_item');

CREATE TABLE sales_invoice_tax (
    id BIGSERIAL PRIMARY KEY,
    shop_code INTEGER NOT NULL
        DEFAULT current_setting('app.shop_code', false)::INTEGER,
    sales_invoice_id BIGINT NOT NULL REFERENCES sales_invoice(id),

    tax_type VARCHAR(10) NOT NULL,
    tax_rate NUMERIC(18,2) NOT NULL,
    taxable_amount NUMERIC(18,2) NOT NULL,
    tax_amount NUMERIC(18,2) NOT NULL,
    tax_ledger_id BIGINT NOT NULL REFERENCES ledger(id)
);

CREATE INDEX idx_sales_invoice_tax_invoice ON sales_invoice_tax(sales_invoice_id);

CALL create_shop_isolation_policy('sales_invoice_tax', 'shop_isolation_sales_invoice_tax');

CREATE TABLE stock_ledger (
    id BIGSERIAL PRIMARY KEY,
    shop_code INTEGER NOT NULL
        DEFAULT current_setting('app.shop_code', false)::INTEGER,

    stock_item_id BIGINT NOT NULL REFERENCES stock_item_master(id),
    godown_id BIGINT,

    transaction_date DATE NOT NULL,
    -- Nullable only for opening balances. Types: OPENING, SALE, SALE_CANCEL, PURCHASE, PROD_IN, PROD_OUT, ADJ.
    voucher_id BIGINT REFERENCES voucher(id),
    transaction_type VARCHAR(30) NOT NULL,

    in_qty NUMERIC(18,4) NOT NULL DEFAULT 0,
    in_alt_qty NUMERIC(18,4) NOT NULL DEFAULT 0,
    in_rate NUMERIC(18,4) NOT NULL DEFAULT 0,
    in_value NUMERIC(18,2) NOT NULL DEFAULT 0,

    out_qty NUMERIC(18,4) NOT NULL DEFAULT 0,
    out_alt_qty NUMERIC(18,4) NOT NULL DEFAULT 0,
    out_rate NUMERIC(18,4) NOT NULL DEFAULT 0,
    out_value NUMERIC(18,2) NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_ledger_item_date ON stock_ledger(shop_code, stock_item_id, transaction_date);
CREATE INDEX idx_stock_ledger_voucher ON stock_ledger(shop_code, voucher_id);

CALL create_shop_isolation_policy('stock_ledger', 'shop_isolation_stock_ledger');
