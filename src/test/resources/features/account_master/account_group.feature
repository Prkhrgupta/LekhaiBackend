Feature: Account Group Master Management

  Scenario: List default seeded account groups from dropdown
    Given a provisioned shop "Crown Enterprise" with admin "crown_grp_admin"
    When an account group dropdown request is sent
    Then the response status should be 200
    And the response dropdown should contain an item with label "Current Assets"
    And the response dropdown should contain an item with label "Sundry Debtors"

  Scenario: Create a custom sub-account group under an existing group
    Given a provisioned shop "Crown Enterprise" with admin "crown_grp_admin"
    When a custom account group is created with name "North Region Debtors" under parent account group "Sundry Debtors"
    Then the response status should be 200
    And the response field "name" should equal "North Region Debtors"
    When an account group dropdown request is sent
    Then the response status should be 200
    And the response dropdown should contain an item with label "North Region Debtors"

  Scenario: Failed account group creation under non-existent parent
    Given a provisioned shop "Crown Enterprise" with admin "crown_grp_admin"
    When a custom account group is created with name "Orphan Group" under invalid parent id 999999
    Then the response status should be 400

  Scenario: Search account group summaries
    Given a provisioned shop "Crown Enterprise" with admin "crown_grp_admin"
    When an account group summary request is sent with search query "Assets"
    Then the response status should be 200

  Scenario: Multi-tenant isolation for custom account groups
    Given a provisioned shop "Shop Alpha Group" with admin "alpha_grp_admin"
    And a provisioned shop "Shop Beta Group" with admin "beta_grp_admin"
    Given the user operates as shop "Shop Alpha Group"
    When a custom account group is created with name "Alpha Custom Liabilities" under parent account group "Current Liabilities"
    Then the response status should be 200
    Given the user operates as shop "Shop Beta Group"
    When an account group dropdown request is sent
    Then the response status should be 200
    And the response dropdown should not contain an item with label "Alpha Custom Liabilities"
