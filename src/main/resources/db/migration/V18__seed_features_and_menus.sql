-- =========================================================================
-- V18: Seed Default Feature Registry and Menu Structure
-- =========================================================================

-- Root 1: Account Master
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES (1, 'ACCOUNT_MASTER', NULL, 'Account Master', 'book', NULL, NULL, 0)
ON CONFLICT DO NOTHING;

-- Sub-branches under Account Master
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (10, 'LEDGER', 1, 'Ledger', 'book-open', NULL, NULL, 0),
    (11, 'BROKER', 1, 'Broker', 'handshake', NULL, NULL, 1),
    (12, 'TRANSPORT', 1, 'Transport', 'truck', NULL, NULL, 2),
    (13, 'AREA', 1, 'Area', 'map-pin', NULL, NULL, 3),
    (14, 'ACCOUNT_GROUP', 1, 'Account Group', 'layers', NULL, NULL, 4),
    (15, 'SHIPPED_TO', 1, 'Shipped To', 'truck', 10, '/account_master/shipped_to/create', 5)
ON CONFLICT DO NOTHING;

-- Leaves under Ledger
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (101, 'CREATE_LEDGER', 10, 'Create Ledger', 'plus-circle', 0, '/account_master/ledger/create', 0),
    (102, 'EDIT_LEDGER', 10, 'Edit Ledger', 'edit', 1, '/account_master/ledger/edit', 1)
ON CONFLICT DO NOTHING;

-- Leaves under Broker
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (111, 'CREATE_BROKER', 11, 'Create Broker', 'plus-circle', 2, '/account_master/broker/create', 0),
    (112, 'EDIT_BROKER', 11, 'Edit Broker', 'edit', 3, '/account_master/broker/edit', 1)
ON CONFLICT DO NOTHING;

-- Leaves under Transport
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (121, 'CREATE_TRANSPORT', 12, 'Create Transport', 'plus-circle', 4, '/account_master/transport/create', 0),
    (122, 'EDIT_TRANSPORT', 12, 'Edit Transport', 'edit', 5, '/account_master/transport/edit', 1)
ON CONFLICT DO NOTHING;

-- Leaves under Area
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (131, 'CREATE_AREA', 13, 'Create Area', 'plus-circle', 6, '/account_master/area/create', 0),
    (132, 'EDIT_AREA', 13, 'Edit Area', 'edit', 7, '/account_master/area/edit', 1)
ON CONFLICT DO NOTHING;

-- Leaves under Account Group
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (141, 'CREATE_ACCOUNT_GROUP', 14, 'Create Group', 'plus-circle', 8, '/account_master/account_group/create', 0),
    (142, 'EDIT_ACCOUNT_GROUP', 14, 'Edit Group', 'edit', 9, '/account_master/account_group/edit', 1)
ON CONFLICT DO NOTHING;


-- Root 2: Inventory Master
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES (2, 'INVENTORY_MASTER', NULL, 'Inventory Master', 'package', NULL, NULL, 1)
ON CONFLICT DO NOTHING;

-- Sub-branches under Inventory Master
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (21, 'COMMODITY', 2, 'Commodity', 'tag', NULL, NULL, 0),
    (22, 'ITEM_CATEGORY', 2, 'Item Category', 'layers', NULL, NULL, 1),
    (23, 'ITEM_FACTORY', 2, 'Item Factory', 'factory', NULL, NULL, 2),
    (24, 'STOCK_ITEM', 2, 'Stock Item', 'box', NULL, NULL, 3),
    (25, 'UOM', 2, 'Unit of Measurement', 'tag', NULL, NULL, 4)
ON CONFLICT DO NOTHING;

-- Leaves under Commodity
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (211, 'CREATE_COMMODITY', 21, 'Create Commodity', 'plus-circle', 11, '/inventory_master/commodity/create', 0),
    (212, 'EDIT_COMMODITY', 21, 'Edit Commodity', 'edit', 12, '/inventory_master/commodity/edit', 1)
ON CONFLICT DO NOTHING;

-- Leaves under Item Category
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (221, 'CREATE_ITEM_CATEGORY', 22, 'Create Category', 'plus-circle', 13, '/inventory_master/item_category/create', 0),
    (222, 'EDIT_ITEM_CATEGORY', 22, 'Edit Category', 'edit', 14, '/inventory_master/item_category/edit', 1)
ON CONFLICT DO NOTHING;

-- Leaves under Item Factory
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (231, 'CREATE_ITEM_FACTORY', 23, 'Create Factory', 'plus-circle', 15, '/inventory_master/item_factory/create', 0),
    (232, 'EDIT_ITEM_FACTORY', 23, 'Edit Factory', 'edit', 16, '/inventory_master/item_factory/edit', 1)
ON CONFLICT DO NOTHING;

-- Leaves under Stock Item
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (241, 'CREATE_STOCK_ITEM', 24, 'Create Stock Item', 'plus-circle', 17, '/inventory_master/stock_item/create', 0),
    (242, 'EDIT_STOCK_ITEM', 24, 'Edit Stock Item', 'edit', 18, '/inventory_master/stock_item/edit', 1)
ON CONFLICT DO NOTHING;

-- Leaves under UOM
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (251, 'CREATE_UOM', 25, 'Create UOM', 'plus-circle', 19, '/inventory_master/uom/create', 0),
    (252, 'EDIT_UOM', 25, 'Edit UOM', 'edit', 20, '/inventory_master/uom/edit', 1)
ON CONFLICT DO NOTHING;


-- Root 3: Vouchers
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES (3, 'VOUCHER', NULL, 'Vouchers', 'credit-card', NULL, NULL, 2)
ON CONFLICT DO NOTHING;

INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (31, 'PAYMENT_VOUCHER', 3, 'Payment Voucher', 'credit-card', 21, '/voucher/payment/create', 0),
    (32, 'RECEIPT_VOUCHER', 3, 'Receipt Voucher', 'receipt', 22, '/voucher/receipt/create', 1),
    (33, 'CONTRA_VOUCHER', 3, 'Contra Voucher', 'repeat', 23, '/voucher/contra/create', 2),
    (34, 'JOURNAL_VOUCHER', 3, 'Journal Voucher', 'book-open', 24, '/voucher/journal/create', 3)
ON CONFLICT DO NOTHING;


-- Root 4: Account Books
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES (4, 'ACCOUNT_BOOKS', NULL, 'Account Books', 'bar-chart', NULL, NULL, 3)
ON CONFLICT DO NOTHING;

INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (41, 'ACCOUNT_LEDGER_BOOK', 4, 'Account Ledger', 'file-text', 25, '/account_books/account_ledger', 0)
ON CONFLICT DO NOTHING;


-- Root 5: Settings
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES (5, 'SETTINGS', NULL, 'Settings', 'settings', NULL, NULL, 4)
ON CONFLICT DO NOTHING;

INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (51, 'SALE_LEDGER_SETTING', 5, 'Sale Ledger Setting', 'settings', 26, '/sale_voucher/sale_ledger_setting/create', 0),
    (52, 'PURCHASE_LEDGER_SETTING', 5, 'Purchase Ledger Setting', 'settings', 27, '/purchase_voucher/purchase_ledger_setting/create', 1),
    (53, 'GENERAL_LEDGER_SETTING', 5, 'General Ledger Setting', 'settings', 28, '/general_settings/general_ledger_setting', 2)
ON CONFLICT DO NOTHING;


-- Root 6: E-Way Bills
INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES (6, 'EWBS', NULL, 'E-Way Bills', 'shield', NULL, NULL, 5)
ON CONFLICT DO NOTHING;

INSERT INTO features (id, feature_key, parent_id, title, icon, bit_position, route, display_order)
VALUES 
    (61, 'EWB_DASHBOARD', 6, 'Transporter EWB', 'truck', 29, '/ewbs/ewbs_dashboard', 0),
    (62, 'EWB_EXPIRED', 6, 'Expired EWBs', 'clock', 30, '/ewbs/ewbs_expiry', 1),
    (63, 'EWB_TODAY', 6, 'Expiring Today', 'clock', 31, '/ewbs/ewbs_expiry_today', 2),
    (64, 'EWB_TOMORROW', 6, 'Expiring Tomorrow', 'clock', 32, '/ewbs/ewbs_expiry_tomorrow', 3)
ON CONFLICT DO NOTHING;

-- Advance sequence to prevent primary key collisions on new inserts
SELECT setval('features_id_seq', (SELECT COALESCE(MAX(id), 1) FROM features));
