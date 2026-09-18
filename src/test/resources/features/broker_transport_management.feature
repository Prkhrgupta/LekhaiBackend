Feature: Broker and Transporter Management

  Background:
    Given the super admin is authenticated
    When the super admin creates a new shop:
      | firmName      | Logistics Hub               |
      | gstIn         | 29CCCCC2222C1Z5             |
      | address       | 99 Transport Way, Bangalore |
      | adminUsername | logisticsadmin              |
      | adminPassword | password123                 |
      | adminName     | Logistics Admin             |
    Then the response status should be 200
    Given the user credentials are "logisticsadmin" and "password123"
    When a login request is sent to "/login"
    Then the response status should be 200
    And the response should contain a valid JWT token
    When a GET request is sent to "/auth/shop-token" with header "shopcode" as "{createdShopCode}"
    Then the response status should be 200
    And the response should contain a valid shop-scoped JWT token

  Scenario: Create, fetch, update, and search Brokers
    When a POST request is sent to "/broker" with body:
      """
      {
        "name": "Sharma Brokerage",
        "phone": "9888877777"
      }
      """
    Then the response status should be 200
    And the response JSON path "name" should equal "Sharma Brokerage"
    And the response JSON path "phone" should equal "9888877777"
    And the response JSON path "id" is saved as "createdBrokerId"
    When a GET request is sent to "/broker/{createdBrokerId}"
    Then the response status should be 200
    And the response JSON path "id" should not be null
    And the response JSON path "name" should equal "Sharma Brokerage"
    And the response JSON path "phone" should equal "9888877777"
    When a PUT request is sent to "/broker/{createdBrokerId}" with body:
      """
      {
        "name": "Sharma Brokerage Services",
        "phone": "9888877778"
      }
      """
    Then the response status should be 200
    And the response JSON path "name" should equal "Sharma Brokerage Services"
    And the response JSON path "phone" should equal "9888877778"
    When a GET request is sent to "/broker"
    Then the response status should be 200
    And the response JSON path "[0].label" should equal "Sharma Brokerage Services"
    When a GET request is sent to "/broker/summary?page=0&size=10"
    Then the response status should be 200
    And the response JSON path "data" should not be null
    And the response JSON path "pagination.totalElements" should not be null

  Scenario: Create, fetch, update, and search Transporters
    When a POST request is sent to "/transport" with body:
      """
      {
        "name": "FastTrack Logistics",
        "phone": "9777766666",
        "gstNo": "29AABCF1234F1Z5"
      }
      """
    Then the response status should be 200
    And the response JSON path "name" should equal "FastTrack Logistics"
    And the response JSON path "phone" should equal "9777766666"
    And the response JSON path "gstNo" should equal "29AABCF1234F1Z5"
    And the response JSON path "id" is saved as "createdTransportId"
    When a GET request is sent to "/transport/{createdTransportId}"
    Then the response status should be 200
    And the response JSON path "id" should not be null
    And the response JSON path "name" should equal "FastTrack Logistics"
    And the response JSON path "phone" should equal "9777766666"
    And the response JSON path "gstNo" should equal "29AABCF1234F1Z5"
    When a PUT request is sent to "/transport/{createdTransportId}" with body:
      """
      {
        "name": "FastTrack Express Logistics",
        "phone": "9777766667",
        "gstNo": "29AABCF1234F1Z5"
      }
      """
    Then the response status should be 200
    And the response JSON path "name" should equal "FastTrack Express Logistics"
    And the response JSON path "phone" should equal "9777766667"
    And the response JSON path "gstNo" should equal "29AABCF1234F1Z5"
    When a GET request is sent to "/transport"
    Then the response status should be 200
    And the response JSON path "[0].label" should equal "FastTrack Express Logistics"
    When a GET request is sent to "/transport/summary?page=0&size=10"
    Then the response status should be 200
    And the response JSON path "data" should not be null
    And the response JSON path "pagination.totalElements" should not be null

  Scenario: Create a ledger linked with Broker and Transporter
    When a POST request is sent to "/broker" with body:
      """
      {
        "name": "Prime Broker",
        "phone": "9998887776"
      }
      """
    Then the response status should be 200
    And the response JSON path "id" is saved as "linkedBrokerId"
    When a POST request is sent to "/transport" with body:
      """
      {
        "name": "Prime Cargo",
        "phone": "9998887775",
        "gstNo": "29XYZAB1234C1Z9"
      }
      """
    Then the response status should be 200
    And the response JSON path "id" is saved as "linkedTransportId"
    When a POST request is sent to "/area" with body:
      """
      {
        "areaName": "Commercial Hub",
        "description": "Logistics Zone"
      }
      """
    Then the response status should be 200
    And the response JSON path "id" is saved as "linkedAreaId"
    When a POST request is sent to "/ledger" with body:
      """
      {
        "name": "Apex Trading Corp",
        "legal_name": "Apex Trading Corporation",
        "account_group": 18,
        "opening_balance": 25000.00,
        "account_entry_type": "CR",
        "credit_limit": 100000.00,
        "area_id": {linkedAreaId},
        "broker_id": {linkedBrokerId},
        "transport_id": {linkedTransportId},
        "pan": "APEXX1234Y",
        "email": "info@apextrading.com",
        "phone_number": 9988776655,
        "gstInDetailsPresent": false,
        "mail_to": {
          "mail_to_line_1": "100 Industrial Area",
          "mail_to_line_2": "Sector 5",
          "mail_to_line_3": "Ring Road"
        },
        "city": "Bengaluru",
        "pin_code": "560001",
        "distance": 15.5,
        "state_and_code": "KA"
      }
      """
    Then the response status should be 200
    And the response JSON path "name" should equal "Apex Trading Corp"
    And the response JSON path "id" is saved as "createdLedgerId"
    When a GET request is sent to "/ledger/{createdLedgerId}"
    Then the response status should be 200
    And the response JSON path "name" should equal "Apex Trading Corp"
    And the response JSON path "pan" should equal "APEXX1234Y"
    And the response JSON path "email" should equal "info@apextrading.com"
