Feature: Commodity Inventory Master Management

  Scenario: Create and retrieve a commodity with HSN and GST rate
    Given a provisioned shop "Shree Fabrics" with admin "shree_com_admin"
    When a commodity is created with name "Silk Fabric Goods" HSN "5007" and GST rate 5.0
    Then the response status should be 200
    And the response field "name" should equal "Silk Fabric Goods"
    And the response field "hsn_sac_code" should equal "5007"
    When the commodity "Silk Fabric Goods" is fetched
    Then the response status should be 200
    And the response field "name" should equal "Silk Fabric Goods"
    And the response field "hsn_sac_code" should equal "5007"

  Scenario: Update and delete a commodity
    Given a provisioned shop "Shree Fabrics" with admin "shree_com_admin"
    When a commodity is created with name "Draft Fabric" HSN "5208" and GST rate 12.0
    Then the response status should be 200
    When the commodity "Draft Fabric" is updated with name "Cotton Fabric 12%" HSN "5208" and GST rate 12.0
    Then the response status should be 200
    And the response field "name" should equal "Cotton Fabric 12%"
    When the commodity "Cotton Fabric 12%" is deleted
    Then the response status should be 204
    When a commodity list request is sent
    Then the response status should be 200
    And the response list should not contain an item with name "Cotton Fabric 12%"

  Scenario: List options and summary search for commodities
    Given a provisioned shop "Shree Fabrics" with admin "shree_com_admin"
    When a commodity is created with name "Embroidery Thread" HSN "5810" and GST rate 18.0
    Then the response status should be 200
    When a commodity list request is sent
    Then the response status should be 200
    When a commodity summary request is sent with search query "Embroidery"
    Then the response status should be 200
    And the response field "data[0].name" should equal "Embroidery Thread"

  Scenario: Multi-tenant isolation for commodities
    Given a provisioned shop "Shop Alpha Com" with admin "alpha_com_admin"
    And a provisioned shop "Shop Beta Com" with admin "beta_com_admin"
    Given the user operates as shop "Shop Alpha Com"
    When a commodity is created with name "Alpha Only Silk" HSN "5004" and GST rate 5.0
    Then the response status should be 200
    Given the user operates as shop "Shop Beta Com"
    When a commodity list request is sent
    Then the response status should be 200
    And the response list should not contain an item with name "Alpha Only Silk"
