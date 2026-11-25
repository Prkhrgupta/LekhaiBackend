Feature: UI Navigation Menu and Top Bar

  Scenario: Authenticated Shop Admin fetches top bar details
    Given a provisioned shop "Balaji Silks" with admin "balaji_menu_admin"
    When a top bar details request is sent
    Then the response status should be 200
    And the response field "firmName" should equal "Balaji Silks"
    And the response field "name" should not be null

  Scenario: Authenticated Shop Admin fetches navigation menu
    Given a provisioned shop "Balaji Silks" with admin "balaji_menu_admin"
    When a menu request is sent
    Then the response status should be 200

  Scenario: Super Admin fetches top bar details
    Given the super admin is authenticated
    When a top bar details request is sent
    Then the response status should be 200
    And the response field "firmName" should equal "SuperAdmin Console"
    And the response field "name" should equal "Super Admin"
