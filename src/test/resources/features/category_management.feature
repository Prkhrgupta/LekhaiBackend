Feature: Category and Access Control Management

  Scenario: Super Admin creates a new business category and lists categories
    Given the super admin is authenticated
    When a POST request is sent to "/api/category/create" with body:
      """
      {
        "categoryName": "PHARMACY_RETAIL"
      }
      """
    Then the response status should be 200
    And the response JSON path "data.category" should equal "PHARMACY_RETAIL"
    When a GET request is sent to "/api/category/list-all"
    Then the response status should be 200
    And the response JSON path "data" should not be null
