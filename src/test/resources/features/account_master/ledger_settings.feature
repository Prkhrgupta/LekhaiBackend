Feature: Ledger Settings and State Master Management

  Scenario: Retrieve Indian states dropdown list
    Given a provisioned shop "Metro Textiles" with admin "metro_sett_admin"
    When a state dropdown request is sent
    Then the response status should be 200
    And the response dropdown should contain an item with label "Gujarat"
    And the response dropdown should contain an item with label "Maharashtra"

  Scenario: Create, fetch, and update a Sale Ledger Setting
    Given a provisioned shop "Metro Textiles" with admin "metro_sett_admin"
    When a ledger is created with name "GST Sales 5%" under account group "Sales Account" with opening balance 0.0 and type "CR"
    Then the response status should be 200
    When a ledger is created with name "Output CGST 2.5%" under account group "Duties & Taxes" with opening balance 0.0 and type "CR"
    Then the response status should be 200
    When a ledger is created with name "Output SGST 2.5%" under account group "Duties & Taxes" with opening balance 0.0 and type "CR"
    Then the response status should be 200
    When a ledger is created with name "Output IGST 5%" under account group "Duties & Taxes" with opening balance 0.0 and type "CR"
    Then the response status should be 200
    When a sale ledger setting is created for sale ledger "GST Sales 5%" with sale type "IN_STATE" rate 5.0 cgst "Output CGST 2.5%" sgst "Output SGST 2.5%" igst "Output IGST 5%"
    Then the response status should be 200
    And the response field "saleLedgerName" should equal "GST Sales 5%"
    And the response field "saleType" should equal "IN_STATE"
    When the sale ledger setting for "GST Sales 5%" is fetched
    Then the response status should be 200
    And the response field "saleLedgerName" should equal "GST Sales 5%"
    When the sale ledger setting for "GST Sales 5%" is deleted
    Then the response status should be 204

  Scenario: Dropdown options and summary for sale ledger settings
    Given a provisioned shop "Metro Textiles" with admin "metro_sett_admin"
    When a ledger is created with name "Interstate Sales 12%" under account group "Sales Account" with opening balance 0.0 and type "CR"
    Then the response status should be 200
    When a ledger is created with name "IGST 12%" under account group "Duties & Taxes" with opening balance 0.0 and type "CR"
    Then the response status should be 200
    When a sale ledger setting is created for sale ledger "Interstate Sales 12%" with sale type "OUT_STATE" rate 12.0 cgst "" sgst "" igst "IGST 12%"
    Then the response status should be 200
    When a sale ledger setting dropdown request is sent
    Then the response status should be 200
    And the response dropdown should contain an item with label "Interstate Sales 12%"
    When a sale ledger setting summary request is sent
    Then the response status should be 200

  Scenario: Query current balance of a ledger with opening balance
    Given a provisioned shop "Metro Textiles" with admin "metro_sett_admin"
    When a ledger is created with name "Capital Account Owner" under account group "Sundry Creditors" with opening balance 50000.0 and type "CR"
    Then the response status should be 200
    When the ledger "Capital Account Owner" balance is requested
    Then the response status should be 200
    And the response field "current_balance_type" should equal "CR"

  Scenario: Multi-tenant isolation for sale ledger settings
    Given a provisioned shop "Shop Alpha Setting" with admin "alpha_sett_admin"
    And a provisioned shop "Shop Beta Setting" with admin "beta_sett_admin"
    Given the user operates as shop "Shop Alpha Setting"
    When a ledger is created with name "Alpha Only Sales" under account group "Sales Account" with opening balance 0.0 and type "CR"
    Then the response status should be 200
    When a sale ledger setting is created for sale ledger "Alpha Only Sales" with sale type "IN_STATE" rate 5.0 cgst "" sgst "" igst ""
    Then the response status should be 200
    Given the user operates as shop "Shop Beta Setting"
    When a sale ledger setting dropdown request is sent
    Then the response status should be 200
    And the response dropdown should not contain an item with label "Alpha Only Sales"
