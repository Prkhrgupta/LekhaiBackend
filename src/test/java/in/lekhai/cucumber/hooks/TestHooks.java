package in.lekhai.cucumber.hooks;

import in.lekhai.cucumber.context.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;

public class TestHooks {

    @Autowired
    private TestContext testContext;

    @Before
    public void setUp() {
        testContext.clear();
        // Reset and clean test state before each scenario via test endpoint
        RestAssured.given()
                .when()
                .delete("/api/test/cleanup/all")
                .then()
                .statusCode(200);
    }

    @After
    public void tearDown() {
        testContext.clear();
    }
}
