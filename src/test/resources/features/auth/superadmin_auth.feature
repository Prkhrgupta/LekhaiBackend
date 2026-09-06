Feature: Authentication and Login

  Scenario: Successful login for provisioned Super Admin
    Given the super admin credentials are "admin" and "admin@lekhai.in"
    When a login request is sent to "/login"
    Then the response status should be 200
    And the response should contain a valid JWT token

  Scenario: Failed login with invalid credentials
    Given the user credentials are "invalid_user" and "wrong_password"
    When a login request is sent to "/login"
    Then the response status should be 401

  Scenario: Unauthorized login attempt without credentials
    When a login request is sent without credentials to "/login"
    Then the response status should be 401
