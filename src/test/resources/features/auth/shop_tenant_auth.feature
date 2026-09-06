Feature: Shop and Tenant Management

  Scenario: Super Admin creates a shop tenant and shop admin logs in
    Given the super admin is authenticated
    And the super admin creates a category "SAREE"
    When the super admin creates a new shop:
      | firmName      | Acme Traders            |
      | gstIn         | 29ABCDE1234F1Z5         |
      | address       | 123 Main St, Bangalore  |
      | adminUsername | shopadmin1              |
      | adminPassword | password123             |
      | adminName     | Shop Admin One          |
      | category      | SAREE                   |
    Then the response status should be 200
    Given the user credentials are "shopadmin1" and "password123"
    When a login request is sent to "/login"
    Then the response status should be 200
    And the response should contain a valid JWT token
    And the response should contain shop code in shop menu

  Scenario: Shop Admin obtains a shop-scoped JWT token for a specific shop
    Given the super admin is authenticated
    And the super admin creates a category "SAREE"
    When the super admin creates a new shop:
      | firmName      | Nexus Enterprise        |
      | gstIn         | 27ABCDE5678F1Z2         |
      | address       | 456 Sector Rd, Mumbai   |
      | adminUsername | nexusadmin              |
      | adminPassword | password123             |
      | adminName     | Nexus Admin             |
      | category      | SAREE                   |
    Then the response status should be 200
    Given the user credentials are "nexusadmin" and "password123"
    When a login request is sent to "/login"
    Then the response status should be 200
    And the response should contain a valid JWT token
    When a token request is sent for the created shop
    Then the response status should be 200
    And the response should contain a valid shop-scoped JWT token
