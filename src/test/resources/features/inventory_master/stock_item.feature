Feature: Stock Item Master Management

  Scenario: Create and fetch a stock item with single primary UOM
    Given a provisioned shop "Nexus Apparels" with admin "nexus_stock_admin"
    When a UOM is created with unit name "Piece" and quantity code "PCS"
    Then the response status should be 200
    When a stock item is created with name "Cotton Shirt" and primary UOM "Piece"
    Then the response status should be 200
    And the response field "itemName" should equal "Cotton Shirt"
    When the stock item "Cotton Shirt" is fetched
    Then the response status should be 200
    And the response field "itemName" should equal "Cotton Shirt"

  Scenario: Create stock item with alternate UOM and conversion factor
    Given a provisioned shop "Nexus Apparels" with admin "nexus_stock_admin"
    When a UOM is created with unit name "Piece Unit" and quantity code "PCS"
    Then the response status should be 200
    When a UOM is created with unit name "Box Packing" and quantity code "BOX"
    Then the response status should be 200
    When a stock item is created with name "Boxed Sarees", primary UOM "Piece Unit", alternate UOM "Box Packing", and conversion factor 10.0
    Then the response status should be 200
    And the response field "conversionFactor" should equal "10.0"

  Scenario: Validation error when alternate UOM is provided without conversion factor
    Given a provisioned shop "Nexus Apparels" with admin "nexus_stock_admin"
    When a UOM is created with unit name "Single Piece" and quantity code "PCS"
    Then the response status should be 200
    When a UOM is created with unit name "Carton Box" and quantity code "BOX"
    Then the response status should be 200
    When a stock item is created with name "Invalid Dual Item", primary UOM "Single Piece", alternate UOM "Carton Box", without conversion factor
    Then the response status should be 400

  Scenario: Validation error when alternate UOM matches primary UOM
    Given a provisioned shop "Nexus Apparels" with admin "nexus_stock_admin"
    When a UOM is created with unit name "Identical Unit" and quantity code "PCS"
    Then the response status should be 200
    When a stock item is created with name "Invalid Same Unit", primary UOM "Identical Unit", alternate UOM "Identical Unit", and conversion factor 2.0
    Then the response status should be 400

  Scenario: Dropdown options and summary search for stock items
    Given a provisioned shop "Nexus Apparels" with admin "nexus_stock_admin"
    When a UOM is created with unit name "Metre" and quantity code "MTR"
    Then the response status should be 200
    When a stock item is created with name "Linen Fabric" and primary UOM "Metre"
    Then the response status should be 200
    When a stock item dropdown request is sent
    Then the response status should be 200
    And the response dropdown should contain an item with label "Linen Fabric"
    When a stock item summary request is sent with search text "Linen"
    Then the response status should be 200

  Scenario: Multi-tenant isolation for stock items
    Given a provisioned shop "Shop Alpha Stock" with admin "alpha_stock_admin"
    And a provisioned shop "Shop Beta Stock" with admin "beta_stock_admin"
    Given the user operates as shop "Shop Alpha Stock"
    When a UOM is created with unit name "Alpha Unit" and quantity code "PCS"
    Then the response status should be 200
    When a stock item is created with name "Alpha Secret Item" and primary UOM "Alpha Unit"
    Then the response status should be 200
    Given the user operates as shop "Shop Beta Stock"
    When the stock item "Alpha Secret Item" is fetched
    Then the response status should be 400
    When the stock item "Alpha Secret Item" is deleted
    Then the response status should be 400
    When a stock item dropdown request is sent
    Then the response status should be 200
    And the response dropdown should not contain an item with label "Alpha Secret Item"

  Scenario: Create a composite stock item with item category, factory, commodity, and dual UOM
    Given a provisioned shop "Nexus Apparels" with admin "nexus_stock_admin"
    When a UOM is created with unit name "Box" and quantity code "BOX"
    Then the response status should be 200
    When a UOM is created with unit name "Piece" and quantity code "PCS"
    Then the response status should be 200
    When an item category is created with name "Designer Sarees"
    Then the response status should be 200
    When an item factory is created with name "Surat Weavers Ltd"
    Then the response status should be 200
    When a commodity is created with name "Silk Saree 5%" HSN "5007" and GST rate 5.0
    Then the response status should be 200
    When a composite stock item is created with name "Zari Silk Saree" primary UOM "Box" alternate UOM "Piece" factor 12.0 category "Designer Sarees" factory "Surat Weavers Ltd" commodity "Silk Saree 5%"
    Then the response status should be 200
    And the response field "itemName" should equal "Zari Silk Saree"
    And the response field "itemCategoryName" should equal "Designer Sarees"
    And the response field "itemFactoryName" should equal "Surat Weavers Ltd"
    And the response field "commodityName" should equal "Silk Saree 5%"
