Feature: User and Admin Management

  Scenario: Shop Admin registers a new subordinate admin for their shop
    Given the super admin is authenticated
    When the super admin creates a new shop:
      | firmName      | Global Retailers           |
      | gstIn         | 24ABCDE9999F1Z1            |
      | address       | 789 Market Yard, Ahmedabad |
      | adminUsername | owneradmin                 |
      | adminPassword | password123                |
      | adminName     | Owner Admin                |
    Then the response status should be 200
    Given the user credentials are "owneradmin" and "password123"
    When a login request is sent to "/login"
    Then the response status should be 200
    And the response should contain a valid JWT token
    When a GET request is sent to "/auth/shop-token" with header "shopcode" as "{createdShopCode}"
    Then the response status should be 200
    And the response should contain a valid shop-scoped JWT token
    When the admin registers another admin:
      | username | assistantadmin  |
      | password | password123     |
      | name     | Assistant Admin |
      | category | SAREE           |
    Then the response status should be 200
