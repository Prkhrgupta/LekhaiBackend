package in.lekhai.cucumber.steps;

import in.lekhai.cucumber.context.TestContext;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountMasterSteps {

    @Autowired
    private TestContext testContext;

    @When("an account group dropdown request is sent")
    public void anAccountGroupDropdownRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/account-group");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200) {
            List<Map<String, Object>> items = response.jsonPath().getList("$");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    Object label = item.get("label");
                    Object id = item.get("id");
                    if (label != null && id != null) {
                        testContext.set("accountGroupId:" + label.toString(), Long.valueOf(id.toString()));
                    }
                }
            }
        }
    }

    @When("a custom account group is created with name {string} under parent account group {string}")
    public void aCustomAccountGroupIsCreatedUnderParent(String groupName, String parentGroupName) {
        String token = testContext.get("shopScopedToken");
        Long parentId = resolveAccountGroupId(parentGroupName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", groupName);
        requestBody.put("parentId", parentId);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/account-group");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("accountGroupId:" + groupName, id);
            testContext.set("lastCreatedAccountGroupId", id);
        }
    }

    @When("a custom account group is created with name {string} under invalid parent id {long}")
    public void aCustomAccountGroupIsCreatedUnderInvalidParent(String groupName, long parentId) {
        String token = testContext.get("shopScopedToken");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", groupName);
        requestBody.put("parentId", parentId);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/account-group");

        testContext.setResponse(response);
    }

    @When("an account group summary request is sent with search query {string}")
    public void anAccountGroupSummaryRequestIsSentWithSearchQuery(String query) {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("accountGroupSearchableField", "NAME")
                .queryParam("query", query)
                .contentType(ContentType.JSON)
                .when()
                .get("/account-group/summary");

        testContext.setResponse(response);
    }

    @When("a ledger is created with name {string} under account group {string} with opening balance {double} and type {string}")
    public void aLedgerIsCreatedWithOpeningBalance(String ledgerName, String groupName, double balance, String type) {
        String token = testContext.get("shopScopedToken");
        Long groupId = resolveAccountGroupId(groupName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", ledgerName);
        requestBody.put("account_group", groupId);
        requestBody.put("opening_balance", balance);
        requestBody.put("account_entry_type", type);
        requestBody.put("legal_name", ledgerName + " Legal");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/ledger");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("ledgerId:" + ledgerName, id);
            testContext.set("lastCreatedLedgerId", id);
        }
    }

    @When("a ledger is created with name {string} under account group {string} and GSTIN {string}")
    public void aLedgerIsCreatedWithGstin(String ledgerName, String groupName, String gstin) {
        String token = testContext.get("shopScopedToken");
        Long groupId = resolveAccountGroupId(groupName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", ledgerName);
        requestBody.put("account_group", groupId);
        requestBody.put("gst_in_number", gstin);
        requestBody.put("opening_balance", 0.0);
        requestBody.put("account_entry_type", "DR");
        requestBody.put("gstInDetailsPresent", true);

        Map<String, Object> gstDetails = new HashMap<>();
        gstDetails.put("gst_in_uin", gstin);
        gstDetails.put("registration_type", "REGULAR");
        gstDetails.put("party_type", "NOT_APPLICABLE");
        gstDetails.put("is_e_commerce_operator", false);
        requestBody.put("gst_in_details", gstDetails);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/ledger");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("ledgerId:" + ledgerName, id);
            testContext.set("lastCreatedLedgerId", id);
        }
    }

    @When("the ledger {string} is fetched")
    public void theLedgerIsFetched(String ledgerName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveLedgerId(ledgerName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/ledger/" + id);

        testContext.setResponse(response);
    }

    @When("the ledger {string} is updated with name {string}")
    public void theLedgerIsUpdated(String existingName, String newName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveLedgerId(existingName);

        Response existing = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/ledger/" + id);
        Long groupId = null;
        if (existing.getStatusCode() == 200) {
            Object grpIdObj = existing.jsonPath().get("account_group.id");
            if (grpIdObj != null) {
                groupId = Long.valueOf(grpIdObj.toString());
            }
        }
        if (groupId == null) {
            groupId = resolveAccountGroupId("Sundry Debtors");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", newName);
        requestBody.put("account_group", groupId);
        requestBody.put("opening_balance", 0.0);
        requestBody.put("account_entry_type", "DR");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/ledger/" + id);

        testContext.setResponse(response);
        if (response.getStatusCode() == 200) {
            testContext.set("ledgerId:" + newName, id);
        }
    }

    @When("a ledger dropdown request is sent")
    public void aLedgerDropdownRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/ledger");

        testContext.setResponse(response);
    }

    @When("a ledger summary request is sent with search query {string}")
    public void aLedgerSummaryRequestIsSentWithSearchQuery(String query) {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("ledgerSearchableField", "NAME")
                .queryParam("query", query)
                .contentType(ContentType.JSON)
                .when()
                .get("/ledger/summary");

        testContext.setResponse(response);
    }

    @When("a ledger is requested by GSTIN {string}")
    public void aLedgerIsRequestedByGstin(String gstin) {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("gstin", gstin)
                .contentType(ContentType.JSON)
                .when()
                .get("/ledger/by-gstin");

        testContext.setResponse(response);
    }

    @When("the ledger {string} balance is requested")
    public void theLedgerBalanceIsRequested(String ledgerName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveLedgerId(ledgerName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/ledger/" + id + "/balance");

        testContext.setResponse(response);
    }

    private Long resolveAccountGroupId(String nameOrId) {
        Long id = testContext.get("accountGroupId:" + nameOrId);
        if (id != null) {
            return id;
        }
        try {
            return Long.parseLong(nameOrId);
        } catch (NumberFormatException e) {
            // Fetch dropdown if not loaded yet
            anAccountGroupDropdownRequestIsSent();
            id = testContext.get("accountGroupId:" + nameOrId);
            if (id != null) return id;
            throw new IllegalArgumentException("Cannot resolve AccountGroup ID for: " + nameOrId);
        }
    }

    private Long resolveLedgerId(String nameOrId) {
        Long id = testContext.get("ledgerId:" + nameOrId);
        if (id != null) {
            return id;
        }
        try {
            return Long.parseLong(nameOrId);
        } catch (NumberFormatException e) {
            Long lastId = testContext.get("lastCreatedLedgerId");
            if (lastId != null) return lastId;
            throw new IllegalArgumentException("Cannot resolve Ledger ID for: " + nameOrId);
        }
    }

    // --- Area Steps ---

    @When("an area is created with name {string}")
    public void anAreaIsCreated(String areaName) {
        String token = testContext.get("shopScopedToken");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("areaName", areaName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/area");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("areaId:" + areaName, id);
            testContext.set("lastCreatedAreaId", id);
        }
    }

    @When("the area {string} is fetched")
    public void theAreaIsFetched(String areaName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveAreaId(areaName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/area/" + id);

        testContext.setResponse(response);
    }

    @When("the area {string} is updated with name {string}")
    public void theAreaIsUpdated(String existingName, String newName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveAreaId(existingName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("areaName", newName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/area/" + id);

        testContext.setResponse(response);
        if (response.getStatusCode() == 200) {
            testContext.set("areaId:" + newName, id);
        }
    }

    @When("the area {string} is deleted")
    public void theAreaIsDeleted(String areaName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveAreaId(areaName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .delete("/area/" + id);

        testContext.setResponse(response);
    }

    @When("an area dropdown request is sent")
    public void anAreaDropdownRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/area");

        testContext.setResponse(response);
    }

    @When("an area summary request is sent with search query {string}")
    public void anAreaSummaryRequestIsSent(String query) {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("areaSearchableField", "NAME")
                .queryParam("query", query)
                .contentType(ContentType.JSON)
                .when()
                .get("/area/summary");

        testContext.setResponse(response);
    }

    // --- Broker Steps ---

    @When("a broker is created with name {string} and phone {long}")
    public void aBrokerIsCreated(String name, long phone) {
        String token = testContext.get("shopScopedToken");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", name);
        requestBody.put("phone", phone);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/broker");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("brokerId:" + name, id);
            testContext.set("lastCreatedBrokerId", id);
        }
    }

    @When("the broker {string} is fetched")
    public void theBrokerIsFetched(String name) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveBrokerId(name);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/broker/" + id);

        testContext.setResponse(response);
    }

    @When("the broker {string} is updated with name {string} and phone {long}")
    public void theBrokerIsUpdated(String existingName, String newName, long phone) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveBrokerId(existingName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", newName);
        requestBody.put("phone", phone);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/broker/" + id);

        testContext.setResponse(response);
        if (response.getStatusCode() == 200) {
            testContext.set("brokerId:" + newName, id);
        }
    }

    @When("the broker {string} is deleted")
    public void theBrokerIsDeleted(String name) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveBrokerId(name);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .delete("/broker/" + id);

        testContext.setResponse(response);
    }

    @When("a broker dropdown request is sent")
    public void aBrokerDropdownRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/broker");

        testContext.setResponse(response);
    }

    @When("a broker summary request is sent with search query {string}")
    public void aBrokerSummaryRequestIsSent(String query) {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("brokerSearchableField", "NAME")
                .queryParam("query", query)
                .contentType(ContentType.JSON)
                .when()
                .get("/broker/summary");

        testContext.setResponse(response);
    }

    // --- Transport Steps ---

    @When("a transport is created with name {string} phone {long} and GST {string}")
    public void aTransportIsCreated(String name, long phone, String gstNo) {
        String token = testContext.get("shopScopedToken");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", name);
        requestBody.put("phone", phone);
        requestBody.put("gstNo", gstNo);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/transport");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("transportId:" + name, id);
            testContext.set("lastCreatedTransportId", id);
        }
    }

    @When("the transport {string} is fetched")
    public void theTransportIsFetched(String name) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveTransportId(name);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/transport/" + id);

        testContext.setResponse(response);
    }

    @When("the transport {string} is updated with name {string} phone {long} and GST {string}")
    public void theTransportIsUpdated(String existingName, String newName, long phone, String gstNo) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveTransportId(existingName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", newName);
        requestBody.put("phone", phone);
        requestBody.put("gstNo", gstNo);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/transport/" + id);

        testContext.setResponse(response);
        if (response.getStatusCode() == 200) {
            testContext.set("transportId:" + newName, id);
        }
    }

    @When("the transport {string} is deleted")
    public void theTransportIsDeleted(String name) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveTransportId(name);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .delete("/transport/" + id);

        testContext.setResponse(response);
    }

    @When("a transport dropdown request is sent")
    public void aTransportDropdownRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/transport");

        testContext.setResponse(response);
    }

    @When("a transport summary request is sent with search query {string}")
    public void aTransportSummaryRequestIsSent(String query) {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("transportSearchableField", "NAME")
                .queryParam("query", query)
                .contentType(ContentType.JSON)
                .when()
                .get("/transport/summary");

        testContext.setResponse(response);
    }

    // --- State Steps ---

    @When("a state dropdown request is sent")
    public void aStateDropdownRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/state");

        testContext.setResponse(response);
    }

    // --- Sale Ledger Setting Steps ---

    @When("a sale ledger setting is created for sale ledger {string} with sale type {string} rate {double} cgst {string} sgst {string} igst {string}")
    public void aSaleLedgerSettingIsCreated(
            String saleLedgerName,
            String saleType,
            double rate,
            String cgstName,
            String sgstName,
            String igstName) {
        String token = testContext.get("shopScopedToken");
        Long saleLedgerId = resolveLedgerId(saleLedgerName);
        Long cgstId = cgstName.isEmpty() ? null : resolveLedgerId(cgstName);
        Long sgstId = sgstName.isEmpty() ? null : resolveLedgerId(sgstName);
        Long igstId = igstName.isEmpty() ? null : resolveLedgerId(igstName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("saleLedgerId", saleLedgerId);
        requestBody.put("saleType", saleType);
        requestBody.put("gstRate", rate);
        if (cgstId != null) requestBody.put("cgstLedgerId", cgstId);
        if (sgstId != null) requestBody.put("sgstLedgerId", sgstId);
        if (igstId != null) requestBody.put("igstLedgerId", igstId);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/sale-ledger-setting");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("saleLedgerSettingId:" + saleLedgerName, id);
            testContext.set("lastCreatedSaleLedgerSettingId", id);
        }
    }

    @When("the sale ledger setting for {string} is fetched")
    public void theSaleLedgerSettingIsFetched(String saleLedgerName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveSaleLedgerSettingId(saleLedgerName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/sale-ledger-setting/" + id);

        testContext.setResponse(response);
    }

    @When("the sale ledger setting for {string} is deleted")
    public void theSaleLedgerSettingIsDeleted(String saleLedgerName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveSaleLedgerSettingId(saleLedgerName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .delete("/sale-ledger-setting/" + id);

        testContext.setResponse(response);
    }

    @When("a sale ledger setting dropdown request is sent")
    public void aSaleLedgerSettingDropdownRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/sale-ledger-setting");

        testContext.setResponse(response);
    }

    @When("a sale ledger setting summary request is sent")
    public void aSaleLedgerSettingSummaryRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/sale-ledger-setting/summary");

        testContext.setResponse(response);
    }

    private Long resolveAreaId(String nameOrId) {
        Long id = testContext.get("areaId:" + nameOrId);
        if (id != null) return id;
        try {
            return Long.parseLong(nameOrId);
        } catch (NumberFormatException e) {
            Long lastId = testContext.get("lastCreatedAreaId");
            if (lastId != null) return lastId;
            throw new IllegalArgumentException("Cannot resolve Area ID for: " + nameOrId);
        }
    }

    private Long resolveBrokerId(String nameOrId) {
        Long id = testContext.get("brokerId:" + nameOrId);
        if (id != null) return id;
        try {
            return Long.parseLong(nameOrId);
        } catch (NumberFormatException e) {
            Long lastId = testContext.get("lastCreatedBrokerId");
            if (lastId != null) return lastId;
            throw new IllegalArgumentException("Cannot resolve Broker ID for: " + nameOrId);
        }
    }

    private Long resolveTransportId(String nameOrId) {
        Long id = testContext.get("transportId:" + nameOrId);
        if (id != null) return id;
        try {
            return Long.parseLong(nameOrId);
        } catch (NumberFormatException e) {
            Long lastId = testContext.get("lastCreatedTransportId");
            if (lastId != null) return lastId;
            throw new IllegalArgumentException("Cannot resolve Transport ID for: " + nameOrId);
        }
    }

    // --- Purchase Ledger Setting Steps ---

    @When("a purchase ledger setting is created for purchase ledger {string} with purchase type {string} rate {double} cgst {string} sgst {string} igst {string}")
    public void aPurchaseLedgerSettingIsCreated(
            String purchaseLedgerName,
            String purchaseType,
            double rate,
            String cgstName,
            String sgstName,
            String igstName) {
        String token = testContext.get("shopScopedToken");
        Long purchaseLedgerId = resolveLedgerId(purchaseLedgerName);
        Long cgstId = cgstName.isEmpty() ? null : resolveLedgerId(cgstName);
        Long sgstId = sgstName.isEmpty() ? null : resolveLedgerId(sgstName);
        Long igstId = igstName.isEmpty() ? null : resolveLedgerId(igstName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("purchaseLedgerId", purchaseLedgerId);
        requestBody.put("purchaseType", purchaseType);
        requestBody.put("gstRate", rate);
        if (cgstId != null) requestBody.put("cgstLedgerId", cgstId);
        if (sgstId != null) requestBody.put("sgstLedgerId", sgstId);
        if (igstId != null) requestBody.put("igstLedgerId", igstId);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/purchase-ledger-setting");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("purchaseLedgerSettingId:" + purchaseLedgerName, id);
            testContext.set("lastCreatedPurchaseLedgerSettingId", id);
        }
    }

    @When("the purchase ledger setting for {string} is fetched")
    public void thePurchaseLedgerSettingIsFetched(String purchaseLedgerName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolvePurchaseLedgerSettingId(purchaseLedgerName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/purchase-ledger-setting/" + id);

        testContext.setResponse(response);
    }

    @When("the purchase ledger setting for {string} is deleted")
    public void thePurchaseLedgerSettingIsDeleted(String purchaseLedgerName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolvePurchaseLedgerSettingId(purchaseLedgerName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .delete("/purchase-ledger-setting/" + id);

        testContext.setResponse(response);
    }

    @When("a purchase ledger setting dropdown request is sent")
    public void aPurchaseLedgerSettingDropdownRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/purchase-ledger-setting");

        testContext.setResponse(response);
    }

    @When("a purchase ledger setting summary request is sent")
    public void aPurchaseLedgerSettingSummaryRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/purchase-ledger-setting/summary");

        testContext.setResponse(response);
    }

    // --- General Ledger Setting Steps ---

    @When("the general ledger setting is requested")
    public void theGeneralLedgerSettingIsRequested() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/general-ledger-setting");

        testContext.setResponse(response);
    }

    @When("the general ledger setting is updated with freight {string} round off {string} tds percentage {double} tcs percentage {double}")
    public void theGeneralLedgerSettingIsUpdated(
            String freightLedgerName,
            String roundOffLedgerName,
            double tdsPercentage,
            double tcsPercentage) {
        String token = testContext.get("shopScopedToken");
        Long freightId = freightLedgerName.isEmpty() ? null : resolveLedgerId(freightLedgerName);
        Long roundOffId = roundOffLedgerName.isEmpty() ? null : resolveLedgerId(roundOffLedgerName);

        Map<String, Object> requestBody = new HashMap<>();
        if (freightId != null) requestBody.put("freightPackingLedgerId", freightId);
        if (roundOffId != null) requestBody.put("roundOffLedgerId", roundOffId);
        requestBody.put("tdsPercentage", tdsPercentage);
        requestBody.put("tcsPercentage", tcsPercentage);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/general-ledger-setting");

        testContext.setResponse(response);
    }

    private Long resolvePurchaseLedgerSettingId(String nameOrId) {
        Long id = testContext.get("purchaseLedgerSettingId:" + nameOrId);
        if (id != null) return id;
        try {
            return Long.parseLong(nameOrId);
        } catch (NumberFormatException e) {
            Long lastId = testContext.get("lastCreatedPurchaseLedgerSettingId");
            if (lastId != null) return lastId;
            throw new IllegalArgumentException("Cannot resolve PurchaseLedgerSetting ID for: " + nameOrId);
        }
    }

    private Long resolveSaleLedgerSettingId(String nameOrId) {
        Long id = testContext.get("saleLedgerSettingId:" + nameOrId);
        if (id != null) return id;
        try {
            return Long.parseLong(nameOrId);
        } catch (NumberFormatException e) {
            Long lastId = testContext.get("lastCreatedSaleLedgerSettingId");
            if (lastId != null) return lastId;
            throw new IllegalArgumentException("Cannot resolve SaleLedgerSetting ID for: " + nameOrId);
        }
    }
}
