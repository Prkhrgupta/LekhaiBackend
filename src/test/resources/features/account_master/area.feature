Feature: Area Account Master Management

  Scenario: Create and retrieve an area
    Given a provisioned shop "Metro Textiles" with admin "metro_area_admin"
    When an area is created with name "Ring Road Market"
    Then the response status should be 200
    And the response field "areaName" should equal "Ring Road Market"
    When the area "Ring Road Market" is fetched
    Then the response status should be 200
    And the response field "areaName" should equal "Ring Road Market"

  Scenario: Update an area and verify details
    Given a provisioned shop "Metro Textiles" with admin "metro_area_admin"
    When an area is created with name "Old Market Yard"
    Then the response status should be 200
    When the area "Old Market Yard" is updated with name "Textile Market Yard"
    Then the response status should be 200
    And the response field "areaName" should equal "Textile Market Yard"
    When the area "Textile Market Yard" is fetched
    Then the response status should be 200
    And the response field "areaName" should equal "Textile Market Yard"

  Scenario: Dropdown options and summary search for areas
    Given a provisioned shop "Metro Textiles" with admin "metro_area_admin"
    When an area is created with name "Kala Mandir Area"
    Then the response status should be 200
    When an area dropdown request is sent
    Then the response status should be 200
    And the response dropdown should contain an item with label "Kala Mandir Area"
    When an area summary request is sent with search query "Kala"
    Then the response status should be 200
    And the response field "data[0].areaName" should equal "Kala Mandir Area"

  Scenario: Multi-tenant isolation for areas
    Given a provisioned shop "Shop Alpha Area" with admin "alpha_area_admin"
    And a provisioned shop "Shop Beta Area" with admin "beta_area_admin"
    Given the user operates as shop "Shop Alpha Area"
    When an area is created with name "Alpha Secret Area"
    Then the response status should be 200
    Given the user operates as shop "Shop Beta Area"
    When an area dropdown request is sent
    Then the response status should be 200
    And the response dropdown should not contain an item with label "Alpha Secret Area"
