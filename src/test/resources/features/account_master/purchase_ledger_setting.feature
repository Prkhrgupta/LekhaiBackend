Feature: Purchase Ledger Setting Management

  Scenario: Create, fetch, update, and soft-delete a Purchase Ledger Setting
    Given a provisioned shop "Balaji Silks" with admin "balaji_pur_admin"
    When a ledger is created with name "Yarn Purchase" under account group "Purchase Account" with opening balance 0.0 and type "DR"
    Then the response status should be 200
    When a ledger is created with name "Input CGST 2.5%" under account group "Duties & Taxes" with opening balance 0.0 and type "DR"
    Then the response status should be 200
    When a ledger is created with name "Input SGST 2.5%" under account group "Duties & Taxes" with opening balance 0.0 and type "DR"
    Then the response status should be 200
    When a purchase ledger setting is created for purchase ledger "Yarn Purchase" with purchase type "IN_STATE" rate 5.0 cgst "Input CGST 2.5%" sgst "Input SGST 2.5%" igst ""
    Then the response status should be 200
    And the response field "purchaseLedgerName" should equal "Yarn Purchase"
    When the purchase ledger setting for "Yarn Purchase" is fetched
    Then the response status should be 200
    And the response field "purchaseType" should equal "IN_STATE"
    When the purchase ledger setting for "Yarn Purchase" is deleted
    Then the response status should be 204
    When a purchase ledger setting dropdown request is sent
    Then the response status should be 200
    And the response dropdown should not contain an item with label "Yarn Purchase"

  Scenario: Dropdown options and summary for purchase ledger settings
    Given a provisioned shop "Balaji Silks" with admin "balaji_pur_admin"
    When a ledger is created with name "Interstate Purchase" under account group "Purchase Account" with opening balance 0.0 and type "DR"
    Then the response status should be 200
    When a ledger is created with name "Input IGST 12%" under account group "Duties & Taxes" with opening balance 0.0 and type "DR"
    Then the response status should be 200
    When a purchase ledger setting is created for purchase ledger "Interstate Purchase" with purchase type "OUT_STATE" rate 12.0 cgst "" sgst "" igst "Input IGST 12%"
    Then the response status should be 200
    When a purchase ledger setting dropdown request is sent
    Then the response status should be 200
    And the response dropdown should contain an item with label "Interstate Purchase"
    When a purchase ledger setting summary request is sent
    Then the response status should be 200
    And the response field "data[0].purchaseLedgerName" should equal "Interstate Purchase"

  Scenario: Multi-tenant isolation for purchase ledger settings
    Given a provisioned shop "Shop Alpha Pur" with admin "alpha_pur_admin"
    And a provisioned shop "Shop Beta Pur" with admin "beta_pur_admin"
    Given the user operates as shop "Shop Alpha Pur"
    When a ledger is created with name "Alpha Silk Purchase" under account group "Purchase Account" with opening balance 0.0 and type "DR"
    Then the response status should be 200
    When a purchase ledger setting is created for purchase ledger "Alpha Silk Purchase" with purchase type "IN_STATE" rate 5.0 cgst "" sgst "" igst ""
    Then the response status should be 200
    Given the user operates as shop "Shop Beta Pur"
    When a purchase ledger setting dropdown request is sent
    Then the response status should be 200
    And the response dropdown should not contain an item with label "Alpha Silk Purchase"
