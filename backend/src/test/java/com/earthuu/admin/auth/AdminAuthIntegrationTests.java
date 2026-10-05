package com.earthuu.admin.auth;

import com.earthuu.admin.auth.entity.AdminUser;
import com.earthuu.admin.auth.entity.PasswordCredential;
import com.earthuu.admin.auth.repository.AdminUserRepository;
import com.earthuu.admin.auth.repository.PasswordCredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminAuthIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AdminUserRepository userRepository;

    @Autowired
    PasswordCredentialRepository credentialRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        credentialRepository.deleteAll();
        userRepository.deleteAll();

        var admin = userRepository.save(AdminUser.createAdmin());
        credentialRepository.save(PasswordCredential.create(
                admin.getId(),
                "admin@earthuu.test",
                passwordEncoder.encode("test-password")
        ));
    }

    @Test
    void csrfTokenCanBeIssuedWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/admin/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.headerName").isNotEmpty())
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void adminCanLoginAndReadCurrentSession() throws Exception {
        var result = mockMvc.perform(post("/api/admin/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@earthuu.test",
                                  "password": "test-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("admin@earthuu.test"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andReturn();

        var session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(get("/api/admin/v1/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("admin@earthuu.test"));
    }

    @Test
    void swaggerCanLoginUsingRawXsrfCookieValueAsHeader() throws Exception {
        var csrfResult = mockMvc.perform(get("/api/admin/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        var csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(csrfCookie);

        mockMvc.perform(post("/api/admin/v1/auth/login")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@earthuu.test",
                                  "password": "test-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("admin@earthuu.test"));
    }

    @Test
    void invalidPasswordReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@earthuu.test",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void protectedEndpointRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/admin/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
