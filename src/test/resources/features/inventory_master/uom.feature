Feature: Unit of Measure (UOM) Management

  Scenario: Create and retrieve a Unit of Measure
    Given a provisioned shop "Acme Silks" with admin "acme_uom_admin"
    When a UOM is created with unit name "Gunny Bag" and quantity code "BAG"
    Then the response status should be 200
    And the response field "unit_name" should equal "Gunny Bag"
    And the response field "quantity_code" should equal "BAG"
    When the UOM "Gunny Bag" is fetched
    Then the response status should be 200
    And the response field "unit_name" should equal "Gunny Bag"

  Scenario: List UOM dropdown options and search summaries
    Given a provisioned shop "Acme Silks" with admin "acme_uom_admin"
    When a UOM is created with unit name "Box Pack" and quantity code "BOX"
    Then the response status should be 200
    When a UOM dropdown request is sent
    Then the response status should be 200
    And the response dropdown should contain an item with label "Box Pack"
    When a UOM summary request is sent with search text "Box"
    Then the response status should be 200

  Scenario: Update and delete a Unit of Measure
    Given a provisioned shop "Acme Silks" with admin "acme_uom_admin"
    When a UOM is created with unit name "Temporary Unit" and quantity code "NOS"
    Then the response status should be 200
    When the UOM "Temporary Unit" is updated with unit name "Revised Unit" and quantity code "NOS"
    Then the response status should be 200
    And the response field "unit_name" should equal "Revised Unit"
    When the UOM "Revised Unit" is deleted
    Then the response status should be 204

  Scenario: Multi-tenant isolation for UOM records
    Given a provisioned shop "Shop Alpha" with admin "alpha_uom_admin"
    And a provisioned shop "Shop Beta" with admin "beta_uom_admin"
    Given the user operates as shop "Shop Alpha"
    When a UOM is created with unit name "Exclusive Alpha Metre" and quantity code "MTR"
    Then the response status should be 200
    Given the user operates as shop "Shop Beta"
    When the UOM "Exclusive Alpha Metre" is fetched
    Then the response status should be 400
    When the UOM "Exclusive Alpha Metre" is deleted
    Then the response status should be 400
    When a UOM dropdown request is sent
    Then the response status should be 200
    And the response dropdown should not contain an item with label "Exclusive Alpha Metre"

  Scenario: Cannot delete UOM in use by a stock item
    Given a provisioned shop "Acme Silks" with admin "acme_uom_admin"
    When a UOM is created with unit name "Rolls" and quantity code "ROL"
    Then the response status should be 200
    When a stock item is created with name "Silk Fabric Roll" and primary UOM "Rolls"
    Then the response status should be 200
    When the UOM "Rolls" is deleted
    Then the response status should be 409
