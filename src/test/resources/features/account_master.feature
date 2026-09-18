Feature: Account Masters, Areas, and States

  Background:
    Given the super admin is authenticated
    When the super admin creates a new shop:
      | firmName      | Master Traders        |
      | gstIn         | 29AAAAA0000A1Z5       |
      | address       | 77 Ring Rd, Bengaluru |
      | adminUsername | masteradmin           |
      | adminPassword | password123           |
      | adminName     | Master Admin          |
    Then the response status should be 200
    Given the user credentials are "masteradmin" and "password123"
    When a login request is sent to "/login"
    Then the response status should be 200
    And the response should contain a valid JWT token
    When a GET request is sent to "/auth/shop-token" with header "shopcode" as "{createdShopCode}"
    Then the response status should be 200
    And the response should contain a valid shop-scoped JWT token

  Scenario: Fetch seeded states dropdown
    When a GET request is sent to "/state"
    Then the response status should be 200
    And the response JSON path "[0]" should not be null

  Scenario: Fetch seeded account groups dropdown
    When a GET request is sent to "/account-group"
    Then the response status should be 200
    And the response JSON path "[0]" should not be null

  Scenario: Create and fetch a new business area
    When a POST request is sent to "/area" with body:
      """
      {
        "areaName": "Indiranagar",
        "description": "Commercial Hub Area"
      }
      """
    Then the response status should be 200
    And the response JSON path "areaName" should equal "Indiranagar"
    And the response JSON path "id" is saved as "createdAreaId"
    When a GET request is sent to "/area/{createdAreaId}"
    Then the response status should be 200
    And the response JSON path "areaName" should equal "Indiranagar"
