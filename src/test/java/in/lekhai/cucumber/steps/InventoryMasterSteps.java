package in.lekhai.cucumber.steps;

import in.lekhai.cucumber.context.TestContext;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class InventoryMasterSteps {

    @Autowired
    private TestContext testContext;

    // --- UOM Steps ---

    @When("a UOM is created with unit name {string} and quantity code {string}")
    public void aUomIsCreatedWithUnitNameAndQuantityCode(String unitName, String quantityCode) {
        String token = testContext.get("shopScopedToken");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("unit_name", unitName);
        requestBody.put("quantity_code", quantityCode);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/uom");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("uomId:" + unitName, id);
            testContext.set("lastCreatedUomId", id);
        }
    }

    @When("the UOM {string} is updated with unit name {string} and quantity code {string}")
    public void theUomIsUpdated(String existingName, String newName, String quantityCode) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveUomId(existingName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("unit_name", newName);
        requestBody.put("quantity_code", quantityCode);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/uom/" + id);

        testContext.setResponse(response);
        if (response.getStatusCode() == 200) {
            testContext.set("uomId:" + newName, id);
        }
    }

    @When("the UOM {string} is fetched")
    public void theUomIsFetched(String unitName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveUomId(unitName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/uom/" + id);

        testContext.setResponse(response);
    }

    @When("the UOM {string} is deleted")
    public void theUomIsDeleted(String unitName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveUomId(unitName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .delete("/uom/" + id);

        testContext.setResponse(response);
    }

    @When("a UOM dropdown request is sent")
    public void aUomDropdownRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/uom");

        testContext.setResponse(response);
    }

    @When("a UOM summary request is sent with search query {string}")
    @When("a UOM summary request is sent with search text {string}")
    public void aUomSummaryRequestIsSentWithSearchQuery(String query) {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("uomSearchableField", "UNIT_NAME")
                .queryParam("query", query)
                .contentType(ContentType.JSON)
                .when()
                .get("/uom/summary");

        testContext.setResponse(response);
    }

    // --- Stock Item Steps ---

    @When("a stock item is created with name {string} and primary UOM {string}")
    public void aStockItemIsCreatedWithPrimaryUom(String itemName, String uomName) {
        String token = testContext.get("shopScopedToken");
        Long uomId = resolveUomId(uomName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("itemName", itemName);
        requestBody.put("primaryUomId", uomId);
        requestBody.put("purchasePrice", 500.0);
        requestBody.put("salePrice", 800.0);
        requestBody.put("openingQty", 10.0);
        requestBody.put("openingRate", 500.0);
        requestBody.put("openingValue", 5000.0);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/stock-item");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("stockItemId:" + itemName, id);
            testContext.set("lastCreatedStockItemId", id);
        }
    }

    @When("a stock item is created with name {string} primary UOM {string} alternate UOM {string} and conversion factor {double}")
    @When("a stock item is created with name {string}, primary UOM {string}, alternate UOM {string}, and conversion factor {double}")
    public void aStockItemIsCreatedWithDualUom(String itemName, String primaryUom, String altUom, double factor) {
        String token = testContext.get("shopScopedToken");
        Long primaryId = resolveUomId(primaryUom);
        Long altId = resolveUomId(altUom);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("itemName", itemName);
        requestBody.put("primaryUomId", primaryId);
        requestBody.put("alternateUomId", altId);
        requestBody.put("conversionFactor", factor);
        requestBody.put("purchasePrice", 100.0);
        requestBody.put("salePrice", 150.0);
        requestBody.put("openingQty", 5.0);
        requestBody.put("openingRate", 100.0);
        requestBody.put("openingValue", 500.0);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/stock-item");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("stockItemId:" + itemName, id);
            testContext.set("lastCreatedStockItemId", id);
        }
    }

    @When("a stock item is created with name {string} primary UOM {string} and alternate UOM {string} without conversion factor")
    @When("a stock item is created with name {string}, primary UOM {string}, alternate UOM {string}, without conversion factor")
    public void aStockItemIsCreatedWithMissingConversionFactor(String itemName, String primaryUom, String altUom) {
        String token = testContext.get("shopScopedToken");
        Long primaryId = resolveUomId(primaryUom);
        Long altId = resolveUomId(altUom);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("itemName", itemName);
        requestBody.put("primaryUomId", primaryId);
        requestBody.put("alternateUomId", altId);
        requestBody.put("purchasePrice", 100.0);
        requestBody.put("salePrice", 150.0);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/stock-item");

        testContext.setResponse(response);
    }

    @When("a stock item is created with name {string} primary UOM {string} and alternate UOM {string}")
    public void aStockItemIsCreatedWithSamePrimaryAndAltUom(String itemName, String primaryUom, String altUom) {
        String token = testContext.get("shopScopedToken");
        Long primaryId = resolveUomId(primaryUom);
        Long altId = resolveUomId(altUom);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("itemName", itemName);
        requestBody.put("primaryUomId", primaryId);
        requestBody.put("alternateUomId", altId);
        requestBody.put("conversionFactor", 1.0);
        requestBody.put("purchasePrice", 100.0);
        requestBody.put("salePrice", 150.0);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/stock-item");

        testContext.setResponse(response);
    }

    @When("the stock item {string} is fetched")
    public void theStockItemIsFetched(String itemName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveStockItemId(itemName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/stock-item/" + id);

        testContext.setResponse(response);
    }

    @When("the stock item {string} is deleted")
    public void theStockItemIsDeleted(String itemName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveStockItemId(itemName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .delete("/stock-item/" + id);

        testContext.setResponse(response);
    }

    @When("a stock item dropdown request is sent")
    public void aStockItemDropdownRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/stock-item");

        testContext.setResponse(response);
    }

    @When("a stock item summary request is sent with search text {string}")
    public void aStockItemSummaryRequestIsSentWithSearchText(String searchText) {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("searchableField", "NAME")
                .queryParam("searchText", searchText)
                .contentType(ContentType.JSON)
                .when()
                .get("/stock-item/summary");

        testContext.setResponse(response);
    }

    // --- Item Category Steps ---

    @When("an item category is created with name {string}")
    public void anItemCategoryIsCreated(String name) {
        String token = testContext.get("shopScopedToken");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", name);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/item-category");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("itemCategoryId:" + name, id);
            testContext.set("lastCreatedItemCategoryId", id);
        }
    }

    @When("the item category {string} is fetched")
    public void theItemCategoryIsFetched(String name) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveItemCategoryId(name);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/item-category/" + id);

        testContext.setResponse(response);
    }

    @When("the item category {string} is updated with name {string}")
    public void theItemCategoryIsUpdated(String existingName, String newName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveItemCategoryId(existingName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", newName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/item-category/" + id);

        testContext.setResponse(response);
        if (response.getStatusCode() == 200) {
            testContext.set("itemCategoryId:" + newName, id);
        }
    }

    @When("the item category {string} is deleted")
    public void theItemCategoryIsDeleted(String name) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveItemCategoryId(name);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .delete("/item-category/" + id);

        testContext.setResponse(response);
    }

    @When("an item category dropdown request is sent")
    public void anItemCategoryDropdownRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/item-category");

        testContext.setResponse(response);
    }

    @When("an item category summary request is sent with search query {string}")
    public void anItemCategorySummaryRequestIsSent(String searchQuery) {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("searchableField", "NAME")
                .queryParam("searchText", searchQuery)
                .contentType(ContentType.JSON)
                .when()
                .get("/item-category/summary");

        testContext.setResponse(response);
    }

    // --- Item Factory Steps ---

    @When("an item factory is created with name {string}")
    public void anItemFactoryIsCreated(String name) {
        String token = testContext.get("shopScopedToken");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", name);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/item-factory");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("itemFactoryId:" + name, id);
            testContext.set("lastCreatedItemFactoryId", id);
        }
    }

    @When("the item factory {string} is fetched")
    public void theItemFactoryIsFetched(String name) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveItemFactoryId(name);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/item-factory/" + id);

        testContext.setResponse(response);
    }

    @When("the item factory {string} is updated with name {string}")
    public void theItemFactoryIsUpdated(String existingName, String newName) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveItemFactoryId(existingName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", newName);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/item-factory/" + id);

        testContext.setResponse(response);
        if (response.getStatusCode() == 200) {
            testContext.set("itemFactoryId:" + newName, id);
        }
    }

    @When("the item factory {string} is deleted")
    public void theItemFactoryIsDeleted(String name) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveItemFactoryId(name);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .delete("/item-factory/" + id);

        testContext.setResponse(response);
    }

    @When("an item factory dropdown request is sent")
    public void anItemFactoryDropdownRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/item-factory");

        testContext.setResponse(response);
    }

    @When("an item factory summary request is sent with search query {string}")
    public void anItemFactorySummaryRequestIsSent(String searchQuery) {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("searchableField", "NAME")
                .queryParam("searchText", searchQuery)
                .contentType(ContentType.JSON)
                .when()
                .get("/item-factory/summary");

        testContext.setResponse(response);
    }

    // --- Commodity Steps ---

    @When("a commodity is created with name {string} HSN {string} and GST rate {double}")
    public void aCommodityIsCreated(String name, String hsn, double gstRate) {
        String token = testContext.get("shopScopedToken");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", name);
        requestBody.put("hsn_sac_code", hsn);
        requestBody.put("gst_rate", gstRate);
        requestBody.put("cess_percentage", 0.0);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/commodity");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("commodityId:" + name, id);
            testContext.set("lastCreatedCommodityId", id);
        }
    }

    @When("the commodity {string} is fetched")
    public void theCommodityIsFetched(String name) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveCommodityId(name);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/commodity/" + id);

        testContext.setResponse(response);
    }

    @When("the commodity {string} is updated with name {string} HSN {string} and GST rate {double}")
    public void theCommodityIsUpdated(String existingName, String newName, String hsn, double gstRate) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveCommodityId(existingName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", newName);
        requestBody.put("hsn_sac_code", hsn);
        requestBody.put("gst_rate", gstRate);
        requestBody.put("cess_percentage", 0.0);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/commodity/" + id);

        testContext.setResponse(response);
        if (response.getStatusCode() == 200) {
            testContext.set("commodityId:" + newName, id);
        }
    }

    @When("the commodity {string} is deleted")
    public void theCommodityIsDeleted(String name) {
        String token = testContext.get("shopScopedToken");
        Long id = resolveCommodityId(name);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .delete("/commodity/" + id);

        testContext.setResponse(response);
    }

    @When("a commodity list request is sent")
    public void aCommodityListRequestIsSent() {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/commodity");

        testContext.setResponse(response);
    }

    @When("a commodity summary request is sent with search query {string}")
    public void aCommoditySummaryRequestIsSent(String searchQuery) {
        String token = testContext.get("shopScopedToken");

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("commoditySearchableField", "NAME")
                .queryParam("searchQuery", searchQuery)
                .contentType(ContentType.JSON)
                .when()
                .get("/commodity/summary");

        testContext.setResponse(response);
    }

    // --- Composite Stock Item ---

    @When("a composite stock item is created with name {string} primary UOM {string} alternate UOM {string} factor {double} category {string} factory {string} commodity {string}")
    public void aCompositeStockItemIsCreated(
            String itemName,
            String primaryUom,
            String altUom,
            double factor,
            String categoryName,
            String factoryName,
            String commodityName) {
        String token = testContext.get("shopScopedToken");
        Long primaryId = resolveUomId(primaryUom);
        Long altId = resolveUomId(altUom);
        Long categoryId = resolveItemCategoryId(categoryName);
        Long factoryId = resolveItemFactoryId(factoryName);
        Long commodityId = resolveCommodityId(commodityName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("itemName", itemName);
        requestBody.put("primaryUomId", primaryId);
        requestBody.put("alternateUomId", altId);
        requestBody.put("conversionFactor", factor);
        requestBody.put("itemCategoryId", categoryId);
        requestBody.put("itemFactoryId", factoryId);
        requestBody.put("commodityId", commodityId);
        requestBody.put("purchasePrice", 1200.0);
        requestBody.put("salePrice", 2000.0);
        requestBody.put("openingQty", 10.0);
        requestBody.put("openingRate", 1200.0);
        requestBody.put("openingValue", 12000.0);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/stock-item");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            testContext.set("stockItemId:" + itemName, id);
            testContext.set("lastCreatedStockItemId", id);
        }
    }

    private Long resolveUomId(String nameOrId) {
        Long id = testContext.get("uomId:" + nameOrId);
        if (id != null) {
            return id;
        }
        try {
            return Long.parseLong(nameOrId);
        } catch (NumberFormatException e) {
            Long lastId = testContext.get("lastCreatedUomId");
            if (lastId != null) return lastId;
            throw new IllegalArgumentException("Cannot resolve UOM ID for: " + nameOrId);
        }
    }

    private Long resolveStockItemId(String nameOrId) {
        Long id = testContext.get("stockItemId:" + nameOrId);
        if (id != null) {
            return id;
        }
        try {
            return Long.parseLong(nameOrId);
        } catch (NumberFormatException e) {
            Long lastId = testContext.get("lastCreatedStockItemId");
            if (lastId != null) return lastId;
            throw new IllegalArgumentException("Cannot resolve StockItem ID for: " + nameOrId);
        }
    }

    private Long resolveItemCategoryId(String nameOrId) {
        Long id = testContext.get("itemCategoryId:" + nameOrId);
        if (id != null) return id;
        try {
            return Long.parseLong(nameOrId);
        } catch (NumberFormatException e) {
            Long last = testContext.get("lastCreatedItemCategoryId");
            if (last != null) return last;
            throw new IllegalArgumentException("Cannot resolve ItemCategory ID for: " + nameOrId);
        }
    }

    private Long resolveItemFactoryId(String nameOrId) {
        Long id = testContext.get("itemFactoryId:" + nameOrId);
        if (id != null) return id;
        try {
            return Long.parseLong(nameOrId);
        } catch (NumberFormatException e) {
            Long last = testContext.get("lastCreatedItemFactoryId");
            if (last != null) return last;
            throw new IllegalArgumentException("Cannot resolve ItemFactory ID for: " + nameOrId);
        }
    }

    private Long resolveCommodityId(String nameOrId) {
        Long id = testContext.get("commodityId:" + nameOrId);
        if (id != null) return id;
        try {
            return Long.parseLong(nameOrId);
        } catch (NumberFormatException e) {
            Long last = testContext.get("lastCreatedCommodityId");
            if (last != null) return last;
            throw new IllegalArgumentException("Cannot resolve Commodity ID for: " + nameOrId);
        }
    }
}
