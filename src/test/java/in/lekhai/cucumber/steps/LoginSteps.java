package in.lekhai.cucumber.steps;

import com.jayway.jsonpath.JsonPath;
import in.lekhai.cucumber.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LoginSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScenarioContext context;

    // ── Given ───────────────────────────────────────────────

    @Given("the Super Admin logs in with valid credentials")
    public void superAdminLogsIn() throws Exception {
        MvcResult result = mockMvc.perform(get("/login")
                        .with(httpBasic("admin", "admin@lekhai.in")))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        context.setAdminToken(JsonPath.read(responseBody, "$.token"));
        context.setLastResult(result);
        context.setLastStatusCode(result.getResponse().getStatus());
    }

    @Given("a user logs in with username {string} and password {string}")
    public void userLogsInWithCredentials(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(get("/login")
                        .with(httpBasic(username, password)))
                .andReturn();

        context.setLastResult(result);
        context.setLastStatusCode(result.getResponse().getStatus());
    }

    @And("the Super Admin creates a category from {string}")
    public void adminCreatesCategory(String payloadPath) throws Exception {
        String payload = new ClassPathResource(payloadPath)
                .getContentAsString(StandardCharsets.UTF_8);

        MvcResult result = mockMvc.perform(post("/api/category/create")
                        .header("Authorization", "Bearer " + context.getAdminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        context.setLastResult(result);
        context.setLastStatusCode(result.getResponse().getStatus());
    }

    @And("the Super Admin creates a shop from {string}")
    public void adminCreatesShop(String payloadPath) throws Exception {
        String payload = new ClassPathResource(payloadPath)
                .getContentAsString(StandardCharsets.UTF_8);

        MvcResult result = mockMvc.perform(post("/api/shop/create")
                        .header("Authorization", "Bearer " + context.getAdminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        context.setLastResult(result);
        context.setLastStatusCode(result.getResponse().getStatus());
    }

    // ── When ────────────────────────────────────────────────

    @When("user {string} logs in with password {string}")
    public void shopUserLogsIn(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(get("/login")
                        .with(httpBasic(username, password)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        context.setUserToken(JsonPath.read(responseBody, "$.token"));
        context.setLastResult(result);
        context.setLastStatusCode(result.getResponse().getStatus());
    }

    @When("the user requests a shop token for the first shop")
    public void userRequestsShopToken() throws Exception {
        // Extract shopCode from the last login result
        String lastBody = context.getLastResult().getResponse().getContentAsString();
        Integer shopCode = JsonPath.read(lastBody, "$.shopMenu[0].shopCode");
        context.setShopCode(shopCode);

        MvcResult result = mockMvc.perform(get("/auth/shop-token")
                        .header("Authorization", "Bearer " + context.getUserToken())
                        .header("shopcode", String.valueOf(shopCode)))
                .andReturn();

        context.setLastResult(result);
        context.setLastStatusCode(result.getResponse().getStatus());
    }

    // ── Then ────────────────────────────────────────────────

    @Then("the response status should be {int}")
    public void responseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, context.getLastStatusCode());
    }

    @And("the response should contain a token")
    public void responseShouldContainToken() throws Exception {
        String body = context.getLastResult().getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.token");
        assert token != null && !token.isEmpty() : "Expected a non-empty token in the response";
    }

    @And("the response should contain a shop list")
    public void responseShouldContainShopList() throws Exception {
        String body = context.getLastResult().getResponse().getContentAsString();
        net.minidev.json.JSONArray shopMenu = JsonPath.read(body, "$.shopMenu");
        assert shopMenu != null && !shopMenu.isEmpty() : "Expected a non-empty shopMenu in the response";
    }
}
