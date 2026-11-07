Feature: Payment and Receipt Voucher Intake Management

  Scenario: Post a payment voucher from Bank account to a Vendor ledger
    Given a provisioned shop "Balaji Silks" with admin "balaji_vouch_admin"
    When a ledger is created with name "HDFC Bank" under account group "Bank Accounts" with opening balance 50000.0 and type "DR"
    Then the response status should be 200
    When a ledger is created with name "Sharma Weavers" under account group "Sundry Creditors" with opening balance 10000.0 and type "CR"
    Then the response status should be 200
    When a payment voucher is posted on "2026-11-01" from payment account "HDFC Bank" to ledger "Sharma Weavers" with amount 5000.0 and narration "Part payment for silk yarn"
    Then the response status should be 200
    And the response field "voucher_number" should not be null

  Scenario: Post a receipt voucher to Cash account from a Customer ledger
    Given a provisioned shop "Balaji Silks" with admin "balaji_vouch_admin"
    When a ledger is created with name "Shop Cash Counter" under account group "Cash in Hand" with opening balance 15000.0 and type "DR"
    Then the response status should be 200
    When a ledger is created with name "Gupta Cloth Store" under account group "Sundry Debtors" with opening balance 8000.0 and type "DR"
    Then the response status should be 200
    When a receipt voucher is posted on "2026-11-01" into receipt account "Shop Cash Counter" from ledger "Gupta Cloth Store" with amount 4000.0 and narration "Invoice advance payment"
    Then the response status should be 200
    And the response field "voucher_number" should not be null

  Scenario: Validation error when payment account is not cash or bank
    Given a provisioned shop "Balaji Silks" with admin "balaji_vouch_admin"
    When a ledger is created with name "Invalid Payer" under account group "Sundry Creditors" with opening balance 0.0 and type "CR"
    Then the response status should be 200
    When a ledger is created with name "Target Vendor" under account group "Sundry Creditors" with opening balance 0.0 and type "CR"
    Then the response status should be 200
    When a payment voucher is posted on "2026-11-01" from payment account "Invalid Payer" to ledger "Target Vendor" with amount 1000.0 and narration "Invalid payment attempt"
    Then the response status should be 400

  Scenario: Multi-tenant isolation for vouchers
    Given a provisioned shop "Shop Alpha Voucher" with admin "alpha_vouch_admin"
    And a provisioned shop "Shop Beta Voucher" with admin "beta_vouch_admin"
    Given the user operates as shop "Shop Alpha Voucher"
    When a ledger is created with name "Alpha Bank" under account group "Bank Accounts" with opening balance 20000.0 and type "DR"
    Then the response status should be 200
    When a ledger is created with name "Alpha Vendor" under account group "Sundry Creditors" with opening balance 5000.0 and type "CR"
    Then the response status should be 200
    When a payment voucher is posted on "2026-11-01" from payment account "Alpha Bank" to ledger "Alpha Vendor" with amount 2500.0 and narration "Alpha settlement"
    Then the response status should be 200
    Given the user operates as shop "Shop Beta Voucher"
    When a payment voucher is posted on "2026-11-01" from payment account "Alpha Bank" to ledger "Alpha Vendor" with amount 1000.0 and narration "Cross tenant attempt"
    Then the response status should be 404
