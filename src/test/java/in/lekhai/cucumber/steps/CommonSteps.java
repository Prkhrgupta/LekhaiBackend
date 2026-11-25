package in.lekhai.cucumber.steps;

import in.lekhai.cucumber.context.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CommonSteps {

    @Autowired
    private TestContext testContext;

    @Given("a provisioned shop {string} with admin {string}")
    public void aProvisionedShopWithAdmin(String shopName, String adminUsername) {
        String gstIn = getGstInForShop(shopName);
        provisionShop(shopName, adminUsername, gstIn);
    }

    @Given("a provisioned shop {string} with admin {string} and GSTIN {string}")
    public void aProvisionedShopWithAdminAndGstin(String shopName, String adminUsername, String gstIn) {
        provisionShop(shopName, adminUsername, gstIn);
    }

    private void provisionShop(String shopName, String adminUsername, String gstIn) {
        // 1. Authenticate super admin
        Response superAdminLogin = RestAssured.given()
                .auth().preemptive().basic("admin", "admin@lekhai.in")
                .contentType(ContentType.JSON)
                .when()
                .get("/login");
        assertThat(superAdminLogin.getStatusCode(), equalTo(200));
        String superAdminToken = superAdminLogin.jsonPath().getString("token");
        testContext.set("superAdminToken", superAdminToken);

        // 2. Ensure category exists
        Map<String, Object> catBody = new HashMap<>();
        catBody.put("categoryName", "SAREE");
        RestAssured.given()
                .auth().oauth2(superAdminToken)
                .contentType(ContentType.JSON)
                .body(catBody)
                .when()
                .post("/api/category/create");

        // 3. Create shop
        Map<String, Object> adminMap = new HashMap<>();
        adminMap.put("username", adminUsername);
        adminMap.put("password", "password123");
        adminMap.put("name", adminUsername + " Admin");
        adminMap.put("category", "SAREE");

        Map<String, Object> shopBody = new HashMap<>();
        shopBody.put("firmName", shopName);
        shopBody.put("gstIn", gstIn);
        shopBody.put("address", "123 Market Rd");
        shopBody.put("isDefault", true);
        shopBody.put("admin", adminMap);

        Response shopResponse = RestAssured.given()
                .auth().oauth2(superAdminToken)
                .contentType(ContentType.JSON)
                .body(shopBody)
                .when()
                .post("/api/shop/create");

        assertThat(shopResponse.getStatusCode(), equalTo(200));
        Integer shopCode = shopResponse.jsonPath().getInt("data.shopCode");
        assertThat(shopCode, notNullValue());

        // 4. Login as shop admin
        Response loginResponse = RestAssured.given()
                .auth().preemptive().basic(adminUsername, "password123")
                .contentType(ContentType.JSON)
                .when()
                .get("/login");
        assertThat(loginResponse.getStatusCode(), equalTo(200));
        String userToken = loginResponse.jsonPath().getString("token");

        // 5. Get shop-scoped token
        Response tokenResponse = RestAssured.given()
                .auth().oauth2(userToken)
                .header("shopcode", shopCode)
                .contentType(ContentType.JSON)
                .when()
                .get("/auth/shop-token");
        assertThat(tokenResponse.getStatusCode(), equalTo(200));
        String shopScopedToken = tokenResponse.jsonPath().getString("token");

        testContext.set("shopCode:" + shopName, shopCode);
        testContext.set("shopToken:" + shopName, shopScopedToken);
        testContext.set("shopScopedToken", shopScopedToken);
        testContext.set("activeShopCode", shopCode);
        testContext.setResponse(tokenResponse);
    }

    @Given("the user operates as shop {string}")
    public void theUserOperatesAsShop(String shopName) {
        String token = testContext.get("shopToken:" + shopName);
        Integer shopCode = testContext.get("shopCode:" + shopName);
        assertThat("Token for shop " + shopName + " should exist", token, notNullValue());
        testContext.set("shopScopedToken", token);
        testContext.set("activeShopCode", shopCode);
    }

    @Then("the response dropdown should contain an item with label {string}")
    public void theResponseDropdownShouldContainAnItemWithLabel(String expectedLabel) {
        Response response = testContext.getResponse();
        List<String> labels = response.jsonPath().getList("label");
        assertThat(labels, notNullValue());
        assertThat(labels, hasItem(expectedLabel));
    }

    @Then("the response dropdown should not contain an item with label {string}")
    public void theResponseDropdownShouldNotContainAnItemWithLabel(String expectedLabel) {
        Response response = testContext.getResponse();
        List<String> labels = response.jsonPath().getList("label");
        if (labels != null) {
            assertThat(labels, not(hasItem(expectedLabel)));
        }
    }

    @Then("the response list should not contain an item with name {string}")
    public void theResponseListShouldNotContainAnItemWithName(String expectedName) {
        Response response = testContext.getResponse();
        List<String> names = response.jsonPath().getList("name");
        if (names != null) {
            assertThat(names, not(hasItem(expectedName)));
        }
    }

    @Then("the response list should contain at least {int} item(s)")
    public void theResponseListShouldContainAtLeastItems(int minCount) {
        Response response = testContext.getResponse();
        List<?> list;
        try {
            list = response.as(List.class);
        } catch (Exception e) {
            list = response.jsonPath().getList("data");
        }
        assertThat(list, notNullValue());
        assertThat(list.size(), greaterThanOrEqualTo(minCount));
    }

    @Then("the response field {string} should contain at least {int} item(s)")
    public void theResponseFieldShouldContainAtLeastItems(String fieldPath, int minCount) {
        Response response = testContext.getResponse();
        List<?> list = response.jsonPath().getList(fieldPath);
        assertThat(list, notNullValue());
        assertThat(list.size(), greaterThanOrEqualTo(minCount));
    }

    @Then("the response field {string} should equal {string}")
    public void theResponseFieldShouldEqual(String fieldPath, String expectedValue) {
        Response response = testContext.getResponse();
        String actualValue = response.jsonPath().getString(fieldPath);
        assertThat(actualValue, equalTo(expectedValue));
    }

    @Then("the response field {string} should not be null")
    public void theResponseFieldShouldNotBeNull(String fieldPath) {
        Response response = testContext.getResponse();
        Object value = response.jsonPath().get(fieldPath);
        assertThat(value, notNullValue());
    }

    private String getGstInForShop(String shopName) {
        if (shopName.contains("Beta") || shopName.contains("Two")) {
            return "27ABCDE5678F1Z2";
        } else if (shopName.contains("Gamma") || shopName.contains("Three")) {
            return "24ABCDE9999F1Z1";
        }
        return "29ABCDE1234F1Z5";
    }
}
