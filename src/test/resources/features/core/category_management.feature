Feature: Super Admin Category Management

  Scenario: Super Admin creates a new business category and lists all categories
    Given the super admin is authenticated
    When the super admin creates a business category "PHARMACY_RETAIL"
    Then the response status should be 200
    And the response field "data.category" should equal "PHARMACY_RETAIL"
    When the super admin requests the list of all business categories
    Then the response status should be 200
    And the response field "data" should not be null
