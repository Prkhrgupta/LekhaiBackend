Feature: User and Admin Management

  Scenario: Shop Admin registers a new subordinate admin for their shop
    Given the super admin is authenticated
    And the super admin creates a category "SAREE"
    When the super admin creates a new shop:
      | firmName      | Global Retailers           |
      | gstIn         | 24ABCDE9999F1Z1            |
      | address       | 789 Market Yard, Ahmedabad |
      | adminUsername | owneradmin                 |
      | adminPassword | password123                |
      | adminName     | Owner Admin                |
      | category      | SAREE                      |
    Then the response status should be 200
    Given the user credentials are "owneradmin" and "password123"
    When a login request is sent to "/login"
    Then the response status should be 200
    And the response should contain a valid JWT token
    When a token request is sent for the created shop
    Then the response status should be 200
    And the response should contain a valid shop-scoped JWT token
    When the admin registers another admin:
      | username | assistantadmin  |
      | password | password123     |
      | name     | Assistant Admin |
      | category | SAREE           |
    Then the response status should be 200
