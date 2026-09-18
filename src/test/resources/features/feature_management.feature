Feature: Feature Flag and Screen Feature Management

  Scenario: Super Admin creates a screen feature and lists all features
    Given the super admin is authenticated
    When a POST request is sent to "/api/feature/create" with body:
      """
      {
        "featureKey": "FEATURE_INVOICE_EXPORT_TEST",
        "title": "Invoice Export Feature",
        "icon": "file-invoice",
        "isScreen": true
      }
      """
    Then the response status should be 200
    And the response JSON path "data.featureKey" should equal "FEATURE_INVOICE_EXPORT_TEST"
    When a GET request is sent to "/api/feature/all"
    Then the response status should be 200
    And the response JSON path "data" should not be null
