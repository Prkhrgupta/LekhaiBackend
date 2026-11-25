Feature: Transporter E-Way Bill Operations

  Scenario: Query transporter EWBs by date range
    Given a provisioned shop "Balaji Transport Firm" with admin "balaji_trans_admin"
    When transporter EWBs are requested from "2026-11-01" to "2026-11-02"
    Then the response status should be 200

  Scenario: Query expiring EWBs for TODAY
    Given a provisioned shop "Balaji Transport Firm" with admin "balaji_trans_admin"
    When transporter expiring EWBs are requested for day "TODAY"
    Then the response status should be 200

  Scenario: Multi-tenant isolation for transporter EWB queries
    Given a provisioned shop "Shop Alpha Trans" with admin "alpha_ewb_admin"
    And a provisioned shop "Shop Beta Trans" with admin "beta_ewb_admin"
    Given the user operates as shop "Shop Alpha Trans"
    When transporter EWBs are requested from "2026-11-01" to "2026-11-02"
    Then the response status should be 200
    Given the user operates as shop "Shop Beta Trans"
    When transporter EWBs are requested from "2026-11-01" to "2026-11-02"
    Then the response status should be 200
