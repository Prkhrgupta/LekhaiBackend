Feature: Account Ledger Statement Report

  Scenario: Account statement reflects opening balance and posted payment voucher
    Given a provisioned shop "Balaji Silks" with admin "balaji_stmt_admin"
    When a ledger is created with name "ICICI Bank" under account group "Bank Accounts" with opening balance 30000.0 and type "DR"
    Then the response status should be 200
    When a ledger is created with name "Kiran Mill" under account group "Sundry Creditors" with opening balance 10000.0 and type "CR"
    Then the response status should be 200
    When a payment voucher is posted on "2026-11-01" from payment account "ICICI Bank" to ledger "Kiran Mill" with amount 4000.0 and narration "Part payment to Kiran Mill"
    Then the response status should be 200
    When an account ledger statement is requested for "Kiran Mill" from "2026-11-01" to "2026-11-02"
    Then the response status should be 200
    And the response list should contain at least 1 items

  Scenario: Multi-tenant isolation for ledger account statements
    Given a provisioned shop "Shop Alpha Statement" with admin "alpha_stmt_admin"
    And a provisioned shop "Shop Beta Statement" with admin "beta_stmt_admin"
    Given the user operates as shop "Shop Alpha Statement"
    When a ledger is created with name "Alpha Secret Ledger" under account group "Sundry Debtors" with opening balance 10000.0 and type "DR"
    Then the response status should be 200
    Given the user operates as shop "Shop Beta Statement"
    When an account ledger statement is requested for "Alpha Secret Ledger" from "2026-11-01" to "2026-11-02"
    Then the response status should be 500
