package in.lekhai.cucumber.steps;

import in.lekhai.cucumber.context.TestContext;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class SystemAuxiliarySteps {

    @Autowired
    private TestContext testContext;

    @When("a top bar details request is sent")
    public void aTopBarDetailsRequestIsSent() {
        String token = resolveActiveToken();

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/menu/topbar");

        testContext.setResponse(response);
    }

    @When("a menu request is sent")
    public void aMenuRequestIsSent() {
        String token = resolveActiveToken();

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get("/menu");

        testContext.setResponse(response);
    }

    @When("GSP credentials are submitted with username {string} and password {string} for GSTIN {string}")
    public void gspCredentialsAreSubmitted(String username, String password, String gstin) {
        String token = resolveActiveToken();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);
        requestBody.put("gstin", gstin);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/gsp/ewb-credentials");

        testContext.setResponse(response);
    }

    @When("transporter EWBs are requested from {string} to {string}")
    public void transporterEwbsAreRequested(String fromDate, String toDate) {
        String token = resolveActiveToken();

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("fromDate", fromDate)
                .queryParam("toDate", toDate)
                .contentType(ContentType.JSON)
                .when()
                .get("/transporter/ewb");

        testContext.setResponse(response);
    }

    @When("transporter expiring EWBs are requested for day {string}")
    public void transporterExpiringEwbsAreRequested(String day) {
        String token = resolveActiveToken();

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("day", day)
                .contentType(ContentType.JSON)
                .when()
                .get("/transporter/ewb/expiring");

        testContext.setResponse(response);
    }

    private String resolveActiveToken() {
        String token = testContext.get("shopScopedToken");
        if (token != null) return token;
        token = testContext.get("authToken");
        if (token != null) return token;
        token = testContext.get("superAdminToken");
        if (token != null) return token;
        throw new IllegalStateException("No active auth token found in TestContext");
    }
}
