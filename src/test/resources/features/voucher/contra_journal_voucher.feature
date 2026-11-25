Feature: Contra and Journal Voucher Intake Management

  Scenario: Post a contra voucher transferring cash to bank
    Given a provisioned shop "Balaji Silks" with admin "balaji_contra_admin"
    When a ledger is created with name "SBI Current Account" under account group "Bank Accounts" with opening balance 10000.0 and type "DR"
    Then the response status should be 200
    When a ledger is created with name "Cash Till" under account group "Cash in Hand" with opening balance 25000.0 and type "DR"
    Then the response status should be 200
    When a contra voucher is posted on "2026-11-01" debiting "SBI Current Account" and crediting "Cash Till" with amount 10000.0 and narration "Cash deposited to SBI"
    Then the response status should be 200
    And the response field "voucher_number" should not be null

  Scenario: Post a balanced journal voucher
    Given a provisioned shop "Balaji Silks" with admin "balaji_contra_admin"
    When a ledger is created with name "Commission Expense" under account group "Indirect Expenses" with opening balance 0.0 and type "DR"
    Then the response status should be 200
    When a ledger is created with name "Broker Payable" under account group "Sundry Creditors" with opening balance 0.0 and type "CR"
    Then the response status should be 200
    When a journal voucher is posted on "2026-11-01" debiting "Commission Expense" and crediting "Broker Payable" with amount 1500.0 and narration "Monthly broker commission"
    Then the response status should be 200
    And the response field "voucher_number" should not be null

  Scenario: Validation error when journal voucher is unbalanced
    Given a provisioned shop "Balaji Silks" with admin "balaji_contra_admin"
    When a ledger is created with name "Misc Expense" under account group "Indirect Expenses" with opening balance 0.0 and type "DR"
    Then the response status should be 200
    When a ledger is created with name "Creditor Party" under account group "Sundry Creditors" with opening balance 0.0 and type "CR"
    Then the response status should be 200
    When an unbalanced journal voucher is posted on "2026-11-01" debiting "Misc Expense" for 1000.0 and crediting "Creditor Party" for 500.0
    Then the response status should be 400
