Feature: General Ledger Setting Management

  Scenario: Initial general ledger setting returns 400 before creation
    Given a provisioned shop "Balaji Silks" with admin "balaji_gl_admin"
    When the general ledger setting is requested
    Then the response status should be 400

  Scenario: Create and fetch general ledger setting
    Given a provisioned shop "Balaji Silks" with admin "balaji_gl_admin"
    When a ledger is created with name "Round Off Account" under account group "Indirect Incomes" with opening balance 0.0 and type "CR"
    Then the response status should be 200
    When a ledger is created with name "Freight Charges" under account group "Direct Expenses" with opening balance 0.0 and type "DR"
    Then the response status should be 200
    When the general ledger setting is updated with freight "Freight Charges" round off "Round Off Account" tds percentage 1.0 tcs percentage 0.1
    Then the response status should be 200
    And the response field "roundOffLedgerName" should equal "Round Off Account"
    And the response field "freightPackingLedgerName" should equal "Freight Charges"
    When the general ledger setting is requested
    Then the response status should be 200
    And the response field "roundOffLedgerName" should equal "Round Off Account"

  Scenario: Multi-tenant isolation for general ledger settings
    Given a provisioned shop "Shop Alpha GL" with admin "alpha_gl_admin"
    And a provisioned shop "Shop Beta GL" with admin "beta_gl_admin"
    Given the user operates as shop "Shop Alpha GL"
    When a ledger is created with name "Alpha Round Off" under account group "Indirect Incomes" with opening balance 0.0 and type "CR"
    Then the response status should be 200
    When the general ledger setting is updated with freight "" round off "Alpha Round Off" tds percentage 0.0 tcs percentage 0.0
    Then the response status should be 200
    Given the user operates as shop "Shop Beta GL"
    When the general ledger setting is requested
    Then the response status should be 400
