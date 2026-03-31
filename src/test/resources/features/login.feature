Feature: Login Authentication

  Scenario: Super Admin logs in successfully
    Given the Super Admin logs in with valid credentials
    Then the response status should be 200
    And the response should contain a token

  Scenario: Login with invalid credentials fails
    Given a user logs in with username "admin" and password "wrongpassword"
    Then the response status should be 401

  Scenario: Shop owner logs in and retrieves a shop token
    Given the Super Admin logs in with valid credentials
    And the Super Admin creates a category from "payloads/category-create.json"
    And the Super Admin creates a shop from "payloads/shop-create-sainath2.json"
    When user "sainath2" logs in with password "password"
    Then the response status should be 200
    And the response should contain a shop list
    When the user requests a shop token for the first shop
    Then the response status should be 200
    And the response should contain a token
