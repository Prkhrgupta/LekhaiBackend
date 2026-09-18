Feature: Voucher Processing and Accounting Entries (Payment, Receipt, Contra, Journal)

  Background:
    Given the super admin is authenticated
    When the super admin creates a new shop:
      | firmName      | Accounting Pro Traders   |
      | gstIn         | 29DDDDD3333D1Z5          |
      | address       | 100 Financial District   |
      | adminUsername | voucheradmin             |
      | adminPassword | password123              |
      | adminName     | Voucher Admin            |
    Then the response status should be 200
    Given the user credentials are "voucheradmin" and "password123"
    When a login request is sent to "/login"
    Then the response status should be 200
    And the response should contain a valid JWT token
    When a GET request is sent to "/auth/shop-token" with header "shopcode" as "{createdShopCode}"
    Then the response status should be 200
    And the response should contain a valid shop-scoped JWT token

    # Setup Area and Ledgers: Bank (19: Bank Accounts), Cash (20: Cash-in-hand), Creditor (26: Sundry Creditors), Debtor (27: Sundry Debtors)
    When a POST request is sent to "/area" with body:
      """
      {
        "areaName": "Commercial District"
      }
      """
    Then the response status should be 200
    And the response JSON path "id" is saved as "vAreaId"

    # Create Bank Account Ledger
    When a POST request is sent to "/ledger" with body:
      """
      {
        "name": "HDFC Current Account",
        "account_group": 19,
        "opening_balance": 500000.00,
        "account_entry_type": "DR",
        "area_id": {vAreaId},
        "gstInDetailsPresent": false,
        "city": "Bengaluru",
        "state_and_code": "KA"
      }
      """
    Then the response status should be 200
    And the response JSON path "id" is saved as "bankLedgerId"

    # Create Cash Ledger
    When a POST request is sent to "/ledger" with body:
      """
      {
        "name": "Main Cash Box",
        "account_group": 20,
        "opening_balance": 50000.00,
        "account_entry_type": "DR",
        "area_id": {vAreaId},
        "gstInDetailsPresent": false,
        "city": "Bengaluru",
        "state_and_code": "KA"
      }
      """
    Then the response status should be 200
    And the response JSON path "id" is saved as "cashLedgerId"

    # Create Supplier Ledger (Sundry Creditors)
    When a POST request is sent to "/ledger" with body:
      """
      {
        "name": "National Yarn Supplier",
        "account_group": 26,
        "opening_balance": 75000.00,
        "account_entry_type": "CR",
        "area_id": {vAreaId},
        "gstInDetailsPresent": false,
        "city": "Surat",
        "state_and_code": "GJ"
      }
      """
    Then the response status should be 200
    And the response JSON path "id" is saved as "supplierLedgerId"

    # Create Customer Ledger (Sundry Debtors)
    When a POST request is sent to "/ledger" with body:
      """
      {
        "name": "City Fashion Retailers",
        "account_group": 27,
        "opening_balance": 120000.00,
        "account_entry_type": "DR",
        "area_id": {vAreaId},
        "gstInDetailsPresent": false,
        "city": "Bengaluru",
        "state_and_code": "KA"
      }
      """
    Then the response status should be 200
    And the response JSON path "id" is saved as "customerLedgerId"

  Scenario: Create a Payment Voucher for Supplier Settlement
    When a POST request is sent to "/payment-voucher" with body:
      """
      {
        "voucher_date": "2026-09-18",
        "payment_account_id": {bankLedgerId},
        "narration": "Payment made via RTGS to National Yarn Supplier",
        "items": [
          {
            "account_id": {supplierLedgerId},
            "amount": 25000.00,
            "remarks": "Invoice settlement part 1"
          }
        ]
      }
      """
    Then the response status should be 200
    And the response JSON path "id" should not be null
    And the response JSON path "voucher_number" should not be null

  Scenario: Create a Receipt Voucher from Customer
    When a POST request is sent to "/receipt-voucher" with body:
      """
      {
        "voucher_date": "2026-09-18",
        "receipt_account_id": {bankLedgerId},
        "narration": "Received advance payment from City Fashion Retailers",
        "items": [
          {
            "account_id": {customerLedgerId},
            "amount": 45000.00,
            "remarks": "Advance for Diwali order"
          }
        ]
      }
      """
    Then the response status should be 200
    And the response JSON path "id" should not be null
    And the response JSON path "voucher_number" should not be null

  Scenario: Create a Contra Voucher for Cash Withdrawal from Bank
    When a POST request is sent to "/contra-voucher" with body:
      """
      {
        "voucher_date": "2026-09-18",
        "narration": "Cash withdrawal from HDFC Bank to Cash Box",
        "debit_entries": [
          {
            "account_id": {cashLedgerId},
            "amount": 10000.00,
            "remarks": "Self cheque withdrawal"
          }
        ],
        "credit_entries": [
          {
            "account_id": {bankLedgerId},
            "amount": 10000.00,
            "remarks": "Bank debited for cash withdrawal"
          }
        ]
      }
      """
    Then the response status should be 200
    And the response JSON path "id" should not be null
    And the response JSON path "voucher_number" should not be null

  Scenario: Create a Journal Voucher and verify Account Ledger Report Statement
    When a POST request is sent to "/journal-voucher" with body:
      """
      {
        "voucher_date": "2026-09-18",
        "narration": "Year-end discount adjustment between parties",
        "debit_entries": [
          {
            "account_id": {supplierLedgerId},
            "amount": 5000.00,
            "remarks": "Debit supplier for volume discount"
          }
        ],
        "credit_entries": [
          {
            "account_id": {customerLedgerId},
            "amount": 5000.00,
            "remarks": "Credit customer pass-through"
          }
        ]
      }
      """
    Then the response status should be 200
    And the response JSON path "id" should not be null
    And the response JSON path "voucher_number" should not be null

    # Verify statement ledger reflection
    When a GET request is sent to "/account-ledger/{supplierLedgerId}?page=0&size=10"
    Then the response status should be 200
    And the response JSON path "data" should not be null
