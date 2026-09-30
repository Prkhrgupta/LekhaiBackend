-- UOM is owned by stock items (stock_item_master.primary_uom_id -> uom_master).
-- The free-text unit on the commodity was an unenforced duplicate source of truth; drop it.
ALTER TABLE commodity_master DROP COLUMN IF EXISTS unit_of_measure;
