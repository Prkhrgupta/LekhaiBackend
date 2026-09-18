package in.lekhai.cucumber.steps;

import in.lekhai.cucumber.context.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CommonHttpSteps {

    @Autowired
    private TestContext testContext;

    @When("a GET request is sent to {string}")
    public void aGetRequestIsSentTo(String path) {
        String resolvedPath = testContext.resolvePlaceholders(path);
        RequestSpecification request = RestAssured.given()
                .contentType(ContentType.JSON);

        String token = testContext.get("token");
        if (token != null) {
            request.auth().oauth2(token);
        }

        Response response = request.when().get(resolvedPath);
        testContext.setResponse(response);
    }

    @When("a GET request is sent to {string} with header {string} as {string}")
    public void aGetRequestIsSentToWithHeader(String path, String headerName, String headerValue) {
        String resolvedPath = testContext.resolvePlaceholders(path);
        String resolvedHeaderValue = testContext.resolvePlaceholders(headerValue);

        RequestSpecification request = RestAssured.given()
                .contentType(ContentType.JSON)
                .header(headerName, resolvedHeaderValue);

        String token = testContext.get("token");
        if (token != null) {
            request.auth().oauth2(token);
        }

        Response response = request.when().get(resolvedPath);
        testContext.setResponse(response);
    }

    @When("a POST request is sent to {string} with body:")
    public void aPostRequestIsSentToWithBody(String path, String docString) {
        String resolvedPath = testContext.resolvePlaceholders(path);
        String resolvedBody = testContext.resolvePlaceholders(docString);

        RequestSpecification request = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(resolvedBody);

        String token = testContext.get("token");
        if (token != null) {
            request.auth().oauth2(token);
        }

        Response response = request.when().post(resolvedPath);
        testContext.setResponse(response);
    }

    @When("a PUT request is sent to {string} with body:")
    public void aPutRequestIsSentToWithBody(String path, String docString) {
        String resolvedPath = testContext.resolvePlaceholders(path);
        String resolvedBody = testContext.resolvePlaceholders(docString);

        RequestSpecification request = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(resolvedBody);

        String token = testContext.get("token");
        if (token != null) {
            request.auth().oauth2(token);
        }

        Response response = request.when().put(resolvedPath);
        testContext.setResponse(response);
    }

    @When("a DELETE request is sent to {string}")
    public void aDeleteRequestIsSentTo(String path) {
        String resolvedPath = testContext.resolvePlaceholders(path);
        RequestSpecification request = RestAssured.given()
                .contentType(ContentType.JSON);

        String token = testContext.get("token");
        if (token != null) {
            request.auth().oauth2(token);
        }

        Response response = request.when().delete(resolvedPath);
        testContext.setResponse(response);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int statusCode) {
        Response response = testContext.getResponse();
        assertThat(response, notNullValue());
        if (response.getStatusCode() != statusCode) {
            System.err.println("=== ASSERTION FAILURE: Expected " + statusCode + " but got " + response.getStatusCode() + " ===");
            System.err.println("Response Body: " + response.getBody().asString());
        }
        assertThat(response.getStatusCode(), equalTo(statusCode));
    }

    @Then("the response JSON path {string} should equal {string}")
    public void theResponseJsonPathShouldEqual(String jsonPath, String expectedValue) {
        Response response = testContext.getResponse();
        assertThat(response.jsonPath().getString(jsonPath), equalTo(expectedValue));
    }

    @Then("the response JSON path {string} should not be null")
    public void theResponseJsonPathShouldNotBeNull(String jsonPath) {
        Response response = testContext.getResponse();
        assertThat(response.jsonPath().get(jsonPath), notNullValue());
    }

    @Then("the response JSON path {string} is saved as {string}")
    public void theResponseJsonPathIsSavedAs(String jsonPath, String contextKey) {
        Response response = testContext.getResponse();
        Object value = response.jsonPath().get(jsonPath);
        testContext.set(contextKey, value);
    }

    @Given("the active token is {string}")
    public void theActiveTokenIs(String tokenKey) {
        String token = testContext.get(tokenKey);
        testContext.set("token", token);
    }
}
