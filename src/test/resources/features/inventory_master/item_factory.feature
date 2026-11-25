Feature: Item Factory Inventory Master Management

  Scenario: Create and retrieve an item factory
    Given a provisioned shop "Shree Fabrics" with admin "shree_fac_admin"
    When an item factory is created with name "Vardhman Weaving Mill"
    Then the response status should be 200
    And the response field "name" should equal "Vardhman Weaving Mill"
    When the item factory "Vardhman Weaving Mill" is fetched
    Then the response status should be 200
    And the response field "name" should equal "Vardhman Weaving Mill"

  Scenario: Update and soft-delete an item factory
    Given a provisioned shop "Shree Fabrics" with admin "shree_fac_admin"
    When an item factory is created with name "Old Surat Mill"
    Then the response status should be 200
    When the item factory "Old Surat Mill" is updated with name "Surat Textile Mill"
    Then the response status should be 200
    And the response field "name" should equal "Surat Textile Mill"
    When the item factory "Surat Textile Mill" is deleted
    Then the response status should be 204
    When an item factory dropdown request is sent
    Then the response status should be 200
    And the response dropdown should not contain an item with label "Surat Textile Mill"

  Scenario: Dropdown options and summary search for item factories
    Given a provisioned shop "Shree Fabrics" with admin "shree_fac_admin"
    When an item factory is created with name "Arvind Mills"
    Then the response status should be 200
    When an item factory dropdown request is sent
    Then the response status should be 200
    And the response dropdown should contain an item with label "Arvind Mills"
    When an item factory summary request is sent with search query "Arvind"
    Then the response status should be 200
    And the response field "data[0].name" should equal "Arvind Mills"

  Scenario: Multi-tenant isolation for item factories
    Given a provisioned shop "Shop Alpha Fac" with admin "alpha_fac_admin"
    And a provisioned shop "Shop Beta Fac" with admin "beta_fac_admin"
    Given the user operates as shop "Shop Alpha Fac"
    When an item factory is created with name "Alpha Only Mill"
    Then the response status should be 200
    Given the user operates as shop "Shop Beta Fac"
    When an item factory dropdown request is sent
    Then the response status should be 200
    And the response dropdown should not contain an item with label "Alpha Only Mill"
