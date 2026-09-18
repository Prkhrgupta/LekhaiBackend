package in.lekhai.cucumber.hooks;

import in.lekhai.cucumber.context.TestContext;
import in.lekhai.test.service.TestCleanupService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

public class TestHooks {

    @LocalServerPort
    private int port;

    @Autowired
    private TestContext testContext;

    @Autowired(required = false)
    private TestCleanupService testCleanupService;

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        testContext.clear();
        if (testCleanupService != null) {
            testCleanupService.cleanupAllTestData();
        }
    }

    @After
    public void tearDown() {
        testContext.clear();
    }
}
