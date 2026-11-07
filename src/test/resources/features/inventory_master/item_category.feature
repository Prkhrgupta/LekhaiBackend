Feature: Item Category Inventory Master Management

  Scenario: Create and retrieve an item category
    Given a provisioned shop "Shree Fabrics" with admin "shree_cat_admin"
    When an item category is created with name "Pure Silk Sarees"
    Then the response status should be 200
    And the response field "name" should equal "Pure Silk Sarees"
    When the item category "Pure Silk Sarees" is fetched
    Then the response status should be 200
    And the response field "name" should equal "Pure Silk Sarees"

  Scenario: Update and soft-delete an item category
    Given a provisioned shop "Shree Fabrics" with admin "shree_cat_admin"
    When an item category is created with name "Linen Sarees"
    Then the response status should be 200
    When the item category "Linen Sarees" is updated with name "Premium Linen Sarees"
    Then the response status should be 200
    And the response field "name" should equal "Premium Linen Sarees"
    When the item category "Premium Linen Sarees" is deleted
    Then the response status should be 204
    When an item category dropdown request is sent
    Then the response status should be 200
    And the response dropdown should not contain an item with label "Premium Linen Sarees"

  Scenario: Dropdown options and summary search for item categories
    Given a provisioned shop "Shree Fabrics" with admin "shree_cat_admin"
    When an item category is created with name "Cotton Suits"
    Then the response status should be 200
    When an item category dropdown request is sent
    Then the response status should be 200
    And the response dropdown should contain an item with label "Cotton Suits"
    When an item category summary request is sent with search query "Cotton"
    Then the response status should be 200
    And the response field "data[0].name" should equal "Cotton Suits"

  Scenario: Multi-tenant isolation for item categories
    Given a provisioned shop "Shop Alpha Cat" with admin "alpha_cat_admin"
    And a provisioned shop "Shop Beta Cat" with admin "beta_cat_admin"
    Given the user operates as shop "Shop Alpha Cat"
    When an item category is created with name "Alpha Exclusive Category"
    Then the response status should be 200
    Given the user operates as shop "Shop Beta Cat"
    When an item category dropdown request is sent
    Then the response status should be 200
    And the response dropdown should not contain an item with label "Alpha Exclusive Category"
