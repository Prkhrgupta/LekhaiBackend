Feature: Super Admin Feature Flag and Screen Feature Management

  Scenario: Super Admin creates a screen feature and lists all features
    Given the super admin is authenticated
    When the super admin creates a screen feature with key "FEATURE_INVOICE_EXPORT_TEST" title "Invoice Export Feature" icon "file-invoice"
    Then the response status should be 200
    And the response field "data.featureKey" should equal "FEATURE_INVOICE_EXPORT_TEST"
    When the super admin requests all screen features
    Then the response status should be 200
    And the response field "data" should not be null
