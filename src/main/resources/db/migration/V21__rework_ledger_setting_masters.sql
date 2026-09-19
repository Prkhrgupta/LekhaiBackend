-- Rework the sale/purchase ledger settings (V18, V20) to match GST/TDS rules:
--  * TCS on sale of goods (206C(1H)) was omitted from 1 Apr 2025, and TDS is a
--    party + nature-of-payment concern (see tds_section / ledger TDS columns),
--    so neither belongs on a sale/purchase ledger.
--  * A purchase is never an EXPORT; it is an IMPORT. Sales gain LUT / SEZ types.
--  * Taxability, ITC eligibility and reverse charge are now captured.
--  * CGST/SGST/IGST percentages are derived from gst_rate by the application;
--    existing rows are recomputed here so stored values are consistent.

-- ---------------------------------------------------------------------------
-- Sale ledger settings
-- ---------------------------------------------------------------------------
ALTER TABLE sale_ledger_setting_master
    DROP COLUMN tcs_percentage,
    DROP COLUMN tcs_ledger_id,
    ADD COLUMN taxability VARCHAR(20) NOT NULL DEFAULT 'TAXABLE'
        CHECK (taxability IN ('TAXABLE', 'EXEMPT', 'NIL_RATED', 'NON_GST'));

ALTER TABLE sale_ledger_setting_master
    DROP CONSTRAINT IF EXISTS sale_ledger_setting_master_sale_type_check;

UPDATE sale_ledger_setting_master SET sale_type = 'EXPORT_UNDER_LUT' WHERE sale_type = 'EXPORT';

ALTER TABLE sale_ledger_setting_master
    ADD CONSTRAINT sale_ledger_setting_master_sale_type_check
        CHECK (sale_type IN ('IN_STATE', 'OUT_STATE', 'EXPORT_WITH_IGST', 'EXPORT_UNDER_LUT',
                             'SEZ_WITH_IGST', 'SEZ_UNDER_LUT'));

UPDATE sale_ledger_setting_master
SET cgst_percentage = CASE WHEN sale_type = 'IN_STATE' THEN COALESCE(gst_rate, 0) / 2 ELSE 0 END,
    sgst_percentage = CASE WHEN sale_type = 'IN_STATE' THEN COALESCE(gst_rate, 0) / 2 ELSE 0 END,
    igst_percentage = CASE WHEN sale_type IN ('OUT_STATE', 'EXPORT_WITH_IGST', 'SEZ_WITH_IGST')
                           THEN COALESCE(gst_rate, 0) ELSE 0 END
WHERE sale_type IS NOT NULL;

-- ---------------------------------------------------------------------------
-- Purchase ledger settings
-- ---------------------------------------------------------------------------
ALTER TABLE purchase_ledger_setting_master
    DROP COLUMN tds_percentage,
    DROP COLUMN tds_ledger_id,
    ADD COLUMN taxability VARCHAR(20) NOT NULL DEFAULT 'TAXABLE'
        CHECK (taxability IN ('TAXABLE', 'EXEMPT', 'NIL_RATED', 'NON_GST')),
    ADD COLUMN itc_eligibility VARCHAR(20) NOT NULL DEFAULT 'ELIGIBLE'
        CHECK (itc_eligibility IN ('ELIGIBLE', 'BLOCKED_17_5', 'INELIGIBLE')),
    ADD COLUMN is_reverse_charge BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN rcm_cgst_payable_ledger_id BIGINT REFERENCES ledger(id),
    ADD COLUMN rcm_sgst_payable_ledger_id BIGINT REFERENCES ledger(id),
    ADD COLUMN rcm_igst_payable_ledger_id BIGINT REFERENCES ledger(id);

ALTER TABLE purchase_ledger_setting_master
    DROP CONSTRAINT IF EXISTS purchase_ledger_setting_master_purchase_type_check;

UPDATE purchase_ledger_setting_master SET purchase_type = 'IMPORT' WHERE purchase_type = 'EXPORT';

ALTER TABLE purchase_ledger_setting_master
    ADD CONSTRAINT purchase_ledger_setting_master_purchase_type_check
        CHECK (purchase_type IN ('IN_STATE', 'OUT_STATE', 'IMPORT', 'SEZ'));

UPDATE purchase_ledger_setting_master
SET cgst_percentage = CASE WHEN purchase_type = 'IN_STATE' THEN COALESCE(gst_rate, 0) / 2 ELSE 0 END,
    sgst_percentage = CASE WHEN purchase_type = 'IN_STATE' THEN COALESCE(gst_rate, 0) / 2 ELSE 0 END,
    igst_percentage = CASE WHEN purchase_type IN ('OUT_STATE', 'IMPORT', 'SEZ')
                           THEN COALESCE(gst_rate, 0) ELSE 0 END
WHERE purchase_type IS NOT NULL;
