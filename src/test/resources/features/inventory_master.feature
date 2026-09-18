Feature: Inventory Master Management (Commodities, Factories, Categories, Stock Items)

  Background:
    Given the super admin is authenticated
    When the super admin creates a new shop:
      | firmName      | Inventory Super Store   |
      | gstIn         | 29CCCCC2222C1Z5         |
      | address       | 99 Silk Board, Bengaluru|
      | adminUsername | inventoryadmin          |
      | adminPassword | password123             |
      | adminName     | Inventory Admin         |
    Then the response status should be 200
    Given the user credentials are "inventoryadmin" and "password123"
    When a login request is sent to "/login"
    Then the response status should be 200
    And the response should contain a valid JWT token
    When a GET request is sent to "/auth/shop-token" with header "shopcode" as "{createdShopCode}"
    Then the response status should be 200
    And the response should contain a valid shop-scoped JWT token

  Scenario: Create and fetch Commodity
    When a POST request is sent to "/commodity" with body:
      """
      {
        "name": "Silk Fabric Premium",
        "hsn_sac_code": "5007",
        "description": "Premium 100% Pure Silk Fabric",
        "gst_rate_sale": 5.0,
        "gst_rate_purchase": 5.0,
        "unit_of_measurement": "MTR"
      }
      """
    Then the response status should be 200
    And the response JSON path "name" should equal "Silk Fabric Premium"
    And the response JSON path "hsn_sac_code" should equal "5007"
    And the response JSON path "id" is saved as "createdCommodityId"
    When a GET request is sent to "/commodity/{createdCommodityId}"
    Then the response status should be 200
    And the response JSON path "name" should equal "Silk Fabric Premium"
    When a GET request is sent to "/commodity"
    Then the response status should be 200
    And the response JSON path "[0].name" should equal "Silk Fabric Premium"
    When a GET request is sent to "/commodity/summary?page=0&size=10"
    Then the response status should be 200
    And the response JSON path "data" should not be null

  Scenario: Create and fetch Item Category
    When a POST request is sent to "/item-category" with body:
      """
      {
        "name": "Printed Sarees"
      }
      """
    Then the response status should be 200
    And the response JSON path "name" should equal "Printed Sarees"
    And the response JSON path "id" is saved as "createdCategoryId"
    When a GET request is sent to "/item-category/{createdCategoryId}"
    Then the response status should be 200
    And the response JSON path "name" should equal "Printed Sarees"
    When a GET request is sent to "/item-category"
    Then the response status should be 200
    And the response JSON path "[0].label" should equal "Printed Sarees"
    When a GET request is sent to "/item-category/summary?page=0&size=10"
    Then the response status should be 200
    And the response JSON path "data" should not be null

  Scenario: Create and fetch Item Factory
    When a POST request is sent to "/item-factory" with body:
      """
      {
        "name": "Surat Weaving Mills",
        "percentage": 2.5
      }
      """
    Then the response status should be 200
    And the response JSON path "name" should equal "Surat Weaving Mills"
    And the response JSON path "id" is saved as "createdFactoryId"
    When a GET request is sent to "/item-factory/{createdFactoryId}"
    Then the response status should be 200
    And the response JSON path "name" should equal "Surat Weaving Mills"
    When a GET request is sent to "/item-factory"
    Then the response status should be 200
    And the response JSON path "[0].label" should equal "Surat Weaving Mills"
    When a GET request is sent to "/item-factory/summary?page=0&size=10"
    Then the response status should be 200
    And the response JSON path "data" should not be null

  Scenario: Create and fetch full Stock Item linked with Commodity, Category, and Factory
    When a POST request is sent to "/commodity" with body:
      """
      {
        "name": "Zari Silk",
        "hsn_sac_code": "500720",
        "gst_rate_sale": 5.0,
        "gst_rate_purchase": 5.0,
        "unit_of_measurement": "MTR"
      }
      """
    Then the response status should be 200
    And the response JSON path "id" is saved as "commId"

    When a POST request is sent to "/item-category" with body:
      """
      {
        "name": "Banarasi Sarees"
      }
      """
    Then the response status should be 200
    And the response JSON path "id" is saved as "catId"

    When a POST request is sent to "/item-factory" with body:
      """
      {
        "name": "Varanasi Handloom Hub",
        "percentage": 3.0
      }
      """
    Then the response status should be 200
    And the response JSON path "id" is saved as "facId"

    When a POST request is sent to "/stock-item" with body:
      """
      {
        "itemName": "Royal Banarasi Silk Saree",
        "itemCategoryId": {catId},
        "itemFactoryId": {facId},
        "commodityId": {commId},
        "finishedRawMaterial": "FINISHED",
        "ratePer": "PCS",
        "purchasePrice": 3500.0,
        "salePrice": 5200.0,
        "openingPcs": 50.0,
        "openingMeter": 275.0,
        "openingRate": 3500.0,
        "openingValue": 175000.0
      }
      """
    Then the response status should be 200
    And the response JSON path "itemName" should equal "Royal Banarasi Silk Saree"
    And the response JSON path "id" is saved as "createdStockItemId"

    When a GET request is sent to "/stock-item/{createdStockItemId}"
    Then the response status should be 200
    And the response JSON path "itemName" should equal "Royal Banarasi Silk Saree"
    And the response JSON path "purchasePrice" should equal "3500.0"

    When a GET request is sent to "/stock-item"
    Then the response status should be 200
    And the response JSON path "[0].label" should equal "Royal Banarasi Silk Saree"

    When a GET request is sent to "/stock-item/summary?page=0&size=10"
    Then the response status should be 200
    And the response JSON path "data" should not be null
