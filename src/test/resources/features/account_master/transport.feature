Feature: Transport Account Master Management

  Scenario: Create and retrieve a transport provider
    Given a provisioned shop "Metro Textiles" with admin "metro_trans_admin"
    When a transport is created with name "Navkar Roadways" phone 9825123456 and GST "24AABCN1234F1Z9"
    Then the response status should be 200
    And the response field "name" should equal "Navkar Roadways"
    When the transport "Navkar Roadways" is fetched
    Then the response status should be 200
    And the response field "name" should equal "Navkar Roadways"

  Scenario: Update a transport provider and verify details
    Given a provisioned shop "Metro Textiles" with admin "metro_trans_admin"
    When a transport is created with name "Old Cargo" phone 9825111111 and GST "24AABCO1111F1Z1"
    Then the response status should be 200
    When the transport "Old Cargo" is updated with name "Express Logistics" phone 9825222222 and GST "24AABCE2222F1Z2"
    Then the response status should be 200
    And the response field "name" should equal "Express Logistics"
    When the transport "Express Logistics" is fetched
    Then the response status should be 200
    And the response field "name" should equal "Express Logistics"

  Scenario: Dropdown options and summary search for transports
    Given a provisioned shop "Metro Textiles" with admin "metro_trans_admin"
    When a transport is created with name "V-Trans India" phone 9825333333 and GST "24AABCV3333F1Z3"
    Then the response status should be 200
    When a transport dropdown request is sent
    Then the response status should be 200
    And the response dropdown should contain an item with label "V-Trans India"
    When a transport summary request is sent with search query "V-Trans"
    Then the response status should be 200
    And the response field "data[0].name" should equal "V-Trans India"

  Scenario: Multi-tenant isolation for transports
    Given a provisioned shop "Shop Alpha Transport" with admin "alpha_trans_admin"
    And a provisioned shop "Shop Beta Transport" with admin "beta_trans_admin"
    Given the user operates as shop "Shop Alpha Transport"
    When a transport is created with name "Alpha Freight" phone 9825444444 and GST "24AABCA4444F1Z4"
    Then the response status should be 200
    Given the user operates as shop "Shop Beta Transport"
    When a transport dropdown request is sent
    Then the response status should be 200
    And the response dropdown should not contain an item with label "Alpha Freight"
