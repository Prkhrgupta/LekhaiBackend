Feature: Ledger Account Master Management

  Scenario: Create and fetch a ledger with opening balance DR
    Given a provisioned shop "Royal Emporium" with admin "royal_ledger_admin"
    When an account group dropdown request is sent
    Then the response status should be 200
    When a ledger is created with name "Kaveri Traders" under account group "Sundry Debtors" with opening balance 15000.0 and type "DR"
    Then the response status should be 200
    And the response field "name" should equal "Kaveri Traders"
    When the ledger "Kaveri Traders" is fetched
    Then the response status should be 200
    And the response field "name" should equal "Kaveri Traders"

  Scenario: Create a ledger with valid GSTIN and query summary
    Given a provisioned shop "Royal Emporium" with admin "royal_ledger_admin"
    When an account group dropdown request is sent
    Then the response status should be 200
    When a ledger is created with name "Surat Silk Supplier" under account group "Sundry Creditors" and GSTIN "24ABCDE9999F1Z1"
    Then the response status should be 200
    When the ledger "Surat Silk Supplier" is fetched
    Then the response status should be 200
    And the response field "name" should equal "Surat Silk Supplier"
    And the response field "gst_in_number" should equal "24ABCDE9999F1Z1"
    When a ledger summary request is sent with search query "Surat"
    Then the response status should be 200
    And the response field "data[0].name" should equal "Surat Silk Supplier"
    And the response field "data[0].gstin" should equal "24ABCDE9999F1Z1"

  Scenario: Duplicate GSTIN constraint within same shop
    Given a provisioned shop "Royal Emporium" with admin "royal_ledger_admin"
    When an account group dropdown request is sent
    Then the response status should be 200
    When a ledger is created with name "Primary Vendor" under account group "Sundry Creditors" and GSTIN "27ABCDE5678F1Z2"
    Then the response status should be 200
    When a ledger is created with name "Duplicate Vendor" under account group "Sundry Creditors" and GSTIN "27ABCDE5678F1Z2"
    Then the response status should be 500

  Scenario: Update ledger and query dropdown options and summaries
    Given a provisioned shop "Royal Emporium" with admin "royal_ledger_admin"
    When an account group dropdown request is sent
    Then the response status should be 200
    When a ledger is created with name "Old Name Party" under account group "Sundry Debtors" with opening balance 5000.0 and type "DR"
    Then the response status should be 200
    When the ledger "Old Name Party" is updated with name "Renamed Party"
    Then the response status should be 200
    And the response field "name" should equal "Renamed Party"
    When a ledger dropdown request is sent
    Then the response status should be 200
    And the response dropdown should contain an item with label "Renamed Party"
    When a ledger summary request is sent with search query "Renamed"
    Then the response status should be 200

  Scenario: Multi-tenant isolation for ledgers
    Given a provisioned shop "Shop Alpha Ledger" with admin "alpha_led_admin"
    And a provisioned shop "Shop Beta Ledger" with admin "beta_led_admin"
    Given the user operates as shop "Shop Alpha Ledger"
    When an account group dropdown request is sent
    Then the response status should be 200
    When a ledger is created with name "Alpha Only Client" under account group "Sundry Debtors" with opening balance 20000.0 and type "DR"
    Then the response status should be 200
    Given the user operates as shop "Shop Beta Ledger"
    When a ledger dropdown request is sent
    Then the response status should be 200
    And the response dropdown should not contain an item with label "Alpha Only Client"
