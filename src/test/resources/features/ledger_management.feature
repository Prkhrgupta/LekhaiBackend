Feature: Ledger Management

  Background:
    Given the super admin is authenticated
    When the super admin creates a new shop:
      | firmName      | Ledger Enterprise       |
      | gstIn         | 29BBBBB1111B1Z5         |
      | address       | 88 Market St, Bangalore |
      | adminUsername | ledgeradmin             |
      | adminPassword | password123             |
      | adminName     | Ledger Admin            |
    Then the response status should be 200
    Given the user credentials are "ledgeradmin" and "password123"
    When a login request is sent to "/login"
    Then the response status should be 200
    And the response should contain a valid JWT token
    When a GET request is sent to "/auth/shop-token" with header "shopcode" as "{createdShopCode}"
    Then the response status should be 200
    And the response should contain a valid shop-scoped JWT token

  Scenario: Create a ledger with all fields and retrieve it by id
    When a POST request is sent to "/area" with body:
      """
      {
        "areaName": "Jayanagar",
        "description": "South Area"
      }
      """
    Then the response status should be 200
    And the response JSON path "id" is saved as "areaId"
    When a POST request is sent to "/ledger" with body:
      """
      {
        "name": "Acme Supplies Private Limited",
        "legal_name": "Acme Supplies Pvt Ltd",
        "account_group": 18,
        "opening_balance": 15000.50,
        "account_entry_type": "CR",
        "credit_limit": 50000.00,
        "area_id": {areaId},
        "pan": "ABCDE1234F",
        "email": "contact@acmesupplies.com",
        "phone_number": 9876543210,
        "gstInDetailsPresent": false,
        "mail_to": {
          "mail_to_line_1": "Plot 42 Industrial Area",
          "mail_to_line_2": "Phase 2",
          "mail_to_line_3": "Near Metro"
        },
        "city": "Bengaluru",
        "pin_code": "560041",
        "distance": 12.0,
        "state_and_code": "KA"
      }
      """
    Then the response status should be 200
    And the response JSON path "name" should equal "Acme Supplies Private Limited"
    And the response JSON path "id" is saved as "createdLedgerId"
    When a GET request is sent to "/ledger/{createdLedgerId}"
    Then the response status should be 200
    And the response JSON path "name" should equal "Acme Supplies Private Limited"
    And the response JSON path "pan" should equal "ABCDE1234F"
    And the response JSON path "email" should equal "contact@acmesupplies.com"

  Scenario: List ledger summaries with pagination and search
    When a GET request is sent to "/ledger/summary?page=0&size=10"
    Then the response status should be 200
    And the response JSON path "data" should not be null
