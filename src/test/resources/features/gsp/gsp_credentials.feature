Feature: GSP Credentials Management

  Scenario: Upsert GSP credentials for a shop tenant
    Given a provisioned shop "Balaji Silks" with admin "balaji_gsp_admin"
    When GSP credentials are submitted with username "gsp_user_1" and password "pass123" for GSTIN "29ABCDE1234F1Z5"
    Then the response status should be 200
    And the response field "gstin" should not be null
    And the response field "message" should not be null

  Scenario: Update existing GSP credentials for the same shop
    Given a provisioned shop "Balaji Silks" with admin "balaji_gsp_admin"
    When GSP credentials are submitted with username "gsp_user_1" and password "pass123" for GSTIN "29ABCDE1234F1Z5"
    Then the response status should be 200
    When GSP credentials are submitted with username "gsp_user_updated" and password "newpass123" for GSTIN "29ABCDE1234F1Z5"
    Then the response status should be 200
    And the response field "gstin" should not be null
