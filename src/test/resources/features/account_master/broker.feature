Feature: Broker Account Master Management

  Scenario: Create and retrieve a broker
    Given a provisioned shop "Metro Textiles" with admin "metro_brok_admin"
    When a broker is created with name "Rajesh Bhai Broker" and phone 9876543210
    Then the response status should be 200
    And the response field "name" should equal "Rajesh Bhai Broker"
    When the broker "Rajesh Bhai Broker" is fetched
    Then the response status should be 200
    And the response field "name" should equal "Rajesh Bhai Broker"

  Scenario: Update a broker and verify details
    Given a provisioned shop "Metro Textiles" with admin "metro_brok_admin"
    When a broker is created with name "Temp Broker" and phone 9123456780
    Then the response status should be 200
    When the broker "Temp Broker" is updated with name "Mukesh Bhai Agency" and phone 9123456789
    Then the response status should be 200
    And the response field "name" should equal "Mukesh Bhai Agency"
    When the broker "Mukesh Bhai Agency" is fetched
    Then the response status should be 200
    And the response field "name" should equal "Mukesh Bhai Agency"

  Scenario: Dropdown options and summary search for brokers
    Given a provisioned shop "Metro Textiles" with admin "metro_brok_admin"
    When a broker is created with name "Vipul Agency" and phone 9988776655
    Then the response status should be 200
    When a broker dropdown request is sent
    Then the response status should be 200
    And the response dropdown should contain an item with label "Vipul Agency"
    When a broker summary request is sent with search query "Vipul"
    Then the response status should be 200
    And the response field "data[0].name" should equal "Vipul Agency"

  Scenario: Multi-tenant isolation for brokers
    Given a provisioned shop "Shop Alpha Broker" with admin "alpha_brok_admin"
    And a provisioned shop "Shop Beta Broker" with admin "beta_brok_admin"
    Given the user operates as shop "Shop Alpha Broker"
    When a broker is created with name "Alpha Only Broker" and phone 9000000001
    Then the response status should be 200
    Given the user operates as shop "Shop Beta Broker"
    When a broker dropdown request is sent
    Then the response status should be 200
    And the response dropdown should not contain an item with label "Alpha Only Broker"
