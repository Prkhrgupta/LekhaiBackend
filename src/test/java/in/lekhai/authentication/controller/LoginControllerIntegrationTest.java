package in.lekhai.authentication.controller;

import in.lekhai.config.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LoginControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testAdminLogin_Success() throws Exception {
        // Admin credentials injected from application.yaml
        String adminUsername = "admin";
        String adminPassword = "admin@lekxxhai.in";

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
}
