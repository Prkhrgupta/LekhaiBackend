-- The ledger/tax configuration moved out of the commodity master into the
-- stand-alone sale_ledger_setting_master / purchase_ledger_setting_master
-- screens (V16, V17). These mappings belong to a sale/purchase ledger account,
-- not to an individual commodity.
ALTER TABLE commodity_master
    DROP COLUMN is_sale_purchase_active,

    DROP COLUMN sale_ac_in_state_id,
    DROP COLUMN sale_cgst_percent,
    DROP COLUMN sale_sgst_percent,
    DROP COLUMN sale_cess_percent,
    DROP COLUMN round_off_ac_id,
    DROP COLUMN sale_ac_out_state_id,
    DROP COLUMN sale_igst_percent,
    DROP COLUMN sale_cess_out_percent,

    DROP COLUMN purchase_ac_in_state_id,
    DROP COLUMN purchase_cgst_percent,
    DROP COLUMN purchase_sgst_percent,
    DROP COLUMN purchase_cess_percent,
    DROP COLUMN purchase_ac_out_state_id,
    DROP COLUMN purchase_igst_percent,
    DROP COLUMN purchase_cess_out_percent;
