package in.lekhai.cucumber.steps;

import in.lekhai.cucumber.context.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AuthSteps {

    @Autowired
    private TestContext testContext;

    private String currentUsername;
    private String currentPassword;

    @Given("the super admin credentials are {string} and {string}")
    public void theSuperAdminCredentialsAre(String username, String password) {
        this.currentUsername = username;
        this.currentPassword = password;
    }

    @Given("the user credentials are {string} and {string}")
    public void theUserCredentialsAre(String username, String password) {
        this.currentUsername = username;
        this.currentPassword = password;
    }

    @Given("the super admin is authenticated")
    public void theSuperAdminIsAuthenticated() {
        Response response = RestAssured.given()
                .auth().preemptive().basic("admin", "admin@lekhai.in")
                .contentType(ContentType.JSON)
                .when()
                .get("/login");

        assertThat(response.getStatusCode(), equalTo(200));
        String token = response.jsonPath().getString("token");
        assertThat(token, notNullValue());
        testContext.set("superAdminToken", token);
        testContext.set("token", token);
    }

    @When("a login request is sent to {string}")
    public void aLoginRequestIsSentTo(String path) {
        Response response = RestAssured.given()
                .auth().preemptive().basic(currentUsername, currentPassword)
                .contentType(ContentType.JSON)
                .when()
                .get(path);

        testContext.setResponse(response);
    }

    @When("a login request is sent without credentials to {string}")
    public void aLoginRequestIsSentWithoutCredentialsTo(String path) {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get(path);

        testContext.setResponse(response);
    }

    @Given("the super admin creates a category {string}")
    public void theSuperAdminCreatesACategory(String categoryName) {
        String superAdminToken = testContext.get("superAdminToken");
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("categoryName", categoryName);

        Response response = RestAssured.given()
                .auth().oauth2(superAdminToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/category/create");

        assertThat(response.getStatusCode(), anyOf(equalTo(200), equalTo(400)));
        testContext.set("categoryName", categoryName);
    }

    @When("the super admin creates a new shop:")
    public void theSuperAdminCreatesANewShopWithCategory(Map<String, String> shopData) {
        String superAdminToken = testContext.get("superAdminToken");

        Map<String, Object> adminMap = new HashMap<>();
        adminMap.put("username", shopData.get("adminUsername"));
        adminMap.put("password", shopData.get("adminPassword"));
        adminMap.put("name", shopData.get("adminName"));
        adminMap.put("category", shopData.get("category"));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firmName", shopData.get("firmName"));
        requestBody.put("gstIn", shopData.get("gstIn"));
        requestBody.put("address", shopData.get("address"));
        requestBody.put("isDefault", true);
        requestBody.put("admin", adminMap);

        Response response = RestAssured.given()
                .auth().oauth2(superAdminToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/shop/create");

        testContext.setResponse(response);
        if (response.getStatusCode() == 200) {
            Integer shopCode = response.jsonPath().getInt("data.shopCode");
            testContext.set("createdShopCode", shopCode);
        }
    }

    @When("a token request is sent for the created shop")
    public void aTokenRequestIsSentForTheCreatedShop() {
        String token = testContext.get("userToken");
        Integer createdShopCode = testContext.get("createdShopCode");
        Response response = RestAssured.given()
                .auth().oauth2(token)
                .header("shopcode", createdShopCode)
                .contentType(ContentType.JSON)
                .when()
                .get("/auth/shop-token");

        testContext.setResponse(response);
    }

    @When("the admin registers another admin:")
    public void theAdminRegistersAnotherAdmin(Map<String, String> adminData) {
        String shopScopedToken = testContext.get("shopScopedToken");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", adminData.get("username"));
        requestBody.put("password", adminData.get("password"));
        requestBody.put("name", adminData.get("name"));
        requestBody.put("category", adminData.get("category"));

        Response response = RestAssured.given()
                .auth().oauth2(shopScopedToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/admin/register/new-admin");

        testContext.setResponse(response);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int statusCode) {
        Response response = testContext.getResponse();
        assertThat(response, notNullValue());
        assertThat(response.getStatusCode(), equalTo(statusCode));
    }

    @Then("the response should contain a valid JWT token")
    public void theResponseShouldContainAValidJwtToken() {
        Response response = testContext.getResponse();
        String token = response.jsonPath().getString("token");
        assertThat(token, notNullValue());
        assertThat(token.split("\\.").length, equalTo(3));
        testContext.set("token", token);
        testContext.set("userToken", token);
    }

    @Then("the response should contain shop code in shop menu")
    public void theResponseShouldContainShopCodeInShopMenu() {
        Response response = testContext.getResponse();
        List<Map<String, Object>> shopMenu = response.jsonPath().getList("shopMenu");
        assertThat(shopMenu, notNullValue());
        assertThat(shopMenu.size(), greaterThan(0));
        Integer createdShopCode = testContext.get("createdShopCode");
        boolean containsShop = shopMenu.stream()
                .anyMatch(menu -> createdShopCode.equals(menu.get("shopCode")));
        assertThat("Shop menu should contain created shopCode", containsShop, is(true));
    }

    @Then("the response should contain a valid shop-scoped JWT token")
    public void theResponseShouldContainAValidShopScopedJwtToken() {
        Response response = testContext.getResponse();
        String token = response.jsonPath().getString("token");
        assertThat(token, notNullValue());
        assertThat(token.split("\\.").length, equalTo(3));
        testContext.set("shopScopedToken", token);
    }
}
