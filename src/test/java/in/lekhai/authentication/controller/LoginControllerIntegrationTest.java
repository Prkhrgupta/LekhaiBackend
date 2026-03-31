package in.lekhai.authentication.controller;

import in.lekhai.config.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import com.jayway.jsonpath.JsonPath;
import org.springframework.core.io.ClassPathResource;
import java.nio.charset.StandardCharsets;

public class LoginControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testAdminLogin_Success() throws Exception {
        // Admin credentials injected from application.yaml
        String adminUsername = "admin";
        String adminPassword = "admin@lekhai.in";

        mockMvc.perform(get("/login")
                .with(httpBasic(adminUsername, adminPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testAdminLogin_Unauthorized() throws Exception {
        mockMvc.perform(get("/login")
                .with(httpBasic("admin", "wrongpassword")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSingleShopLoginFlow() throws Exception {
        // 1. Admin login
        MvcResult adminLoginResult = mockMvc.perform(get("/login")
                .with(httpBasic("admin", "admin@lekhai.in")))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = JsonPath.read(adminLoginResult.getResponse().getContentAsString(), "$.token");

        // 2. Create Category
        String categoryPayload = new ClassPathResource("payloads/category-create.json").getContentAsString(StandardCharsets.UTF_8);
        mockMvc.perform(post("/api/category/create")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryPayload))
                .andExpect(status().is2xxSuccessful());

        // 3. Create Shop
        String shopPayload = new ClassPathResource("payloads/shop-create-sainath2.json").getContentAsString(StandardCharsets.UTF_8);
        mockMvc.perform(post("/api/shop/create")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(shopPayload))
                .andExpect(status().is2xxSuccessful());

        // 4. User Login
        MvcResult userLoginResult = mockMvc.perform(get("/login")
                .with(httpBasic("sainath2", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shopMenu[0].shopCode").exists())
                .andReturn();
        String userToken = JsonPath.read(userLoginResult.getResponse().getContentAsString(), "$.token");
        Integer shopCode = JsonPath.read(userLoginResult.getResponse().getContentAsString(), "$.shopMenu[0].shopCode");

        // 5. Shop level token
        mockMvc.perform(get("/auth/shop-token")
                .header("Authorization", "Bearer " + userToken)
                .header("shopcode", String.valueOf(shopCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
