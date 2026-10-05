package com.earthuu.admin.statistics;

import com.earthuu.admin.auth.entity.AdminUser;
import com.earthuu.admin.auth.entity.PasswordCredential;
import com.earthuu.admin.auth.repository.AdminUserRepository;
import com.earthuu.admin.auth.repository.PasswordCredentialRepository;
import com.earthuu.admin.event.entity.Event;
import com.earthuu.admin.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminStatisticsIntegrationTests {
    private static final String ADMIN_EMAIL = "statistics-admin@earthuu.test";
    @Autowired MockMvc mockMvc;
    @Autowired AdminUserRepository userRepository;
    @Autowired PasswordCredentialRepository credentialRepository;
    @Autowired EventRepository eventRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        var admin = userRepository.save(AdminUser.createAdmin());
        credentialRepository.save(PasswordCredential.create(admin.getId(), ADMIN_EMAIL, passwordEncoder.encode("password")));
        var member = userRepository.save(AdminUser.createUser());
        eventRepository.save(Event.createPending(member.getId()));
        eventRepository.flush();
    }

    @Test
    void overviewAndSeriesUseRequestedPeriodAndFillMissingDates() throws Exception {
        var today = LocalDate.now();
        var from = today.minusDays(2).toString();
        var to = today.toString();
        mockMvc.perform(get("/api/admin/v1/statistics/overview")
                        .with(user(ADMIN_EMAIL).roles("ADMIN"))
                        .queryParam("from", from).queryParam("to", to))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newUsers").value(1))
                .andExpect(jsonPath("$.data.newEvents").value(1));

        mockMvc.perform(get("/api/admin/v1/statistics/users")
                        .with(user(ADMIN_EMAIL).roles("ADMIN"))
                        .queryParam("from", from).queryParam("to", to)
                        .queryParam("granularity", "DAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.registrations.length()").value(3))
                .andExpect(jsonPath("$.data.currentStatusCounts.ACTIVE").value(1));
    }

    @Test
    void eventAndReportSeriesAreAvailable() throws Exception {
        mockMvc.perform(get("/api/admin/v1/statistics/events").with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.created").isArray());
        mockMvc.perform(get("/api/admin/v1/statistics/reports").with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.received").isArray());
    }

    @Test
    void invalidRangeAndAnonymousAccessAreRejected() throws Exception {
        mockMvc.perform(get("/api/admin/v1/statistics/overview")
                        .with(user(ADMIN_EMAIL).roles("ADMIN"))
                        .queryParam("from", "2026-10-05").queryParam("to", "2026-10-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATISTICS_DATE_RANGE"));
        mockMvc.perform(get("/api/admin/v1/statistics/overview")).andExpect(status().isUnauthorized());
    }

    @Test
    void statisticsPathsAreDocumented() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/v1/statistics/overview']").exists())
                .andExpect(jsonPath("$.paths['/api/admin/v1/statistics/users']").exists())
                .andExpect(jsonPath("$.paths['/api/admin/v1/statistics/events']").exists())
                .andExpect(jsonPath("$.paths['/api/admin/v1/statistics/reports']").exists());
    }
}
