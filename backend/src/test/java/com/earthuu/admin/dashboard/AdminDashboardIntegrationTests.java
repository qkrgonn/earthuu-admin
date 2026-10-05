package com.earthuu.admin.dashboard;

import com.earthuu.admin.auth.entity.AdminUser;
import com.earthuu.admin.auth.entity.PasswordCredential;
import com.earthuu.admin.auth.repository.AdminUserRepository;
import com.earthuu.admin.auth.repository.PasswordCredentialRepository;
import com.earthuu.admin.event.entity.Event;
import com.earthuu.admin.event.repository.EventRepository;
import com.earthuu.admin.global.audit.AuditLog;
import com.earthuu.admin.global.audit.AuditLogRepository;
import com.earthuu.admin.report.entity.Report;
import com.earthuu.admin.report.entity.ReportTargetType;
import com.earthuu.admin.report.repository.ReportRepository;
import com.earthuu.admin.verification.entity.StudentVerification;
import com.earthuu.admin.verification.repository.StudentVerificationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminDashboardIntegrationTests {
    private static final String ADMIN_EMAIL = "dashboard-admin@earthuu.test";
    @Autowired MockMvc mockMvc;
    @Autowired AdminUserRepository userRepository;
    @Autowired PasswordCredentialRepository credentialRepository;
    @Autowired EventRepository eventRepository;
    @Autowired ReportRepository reportRepository;
    @Autowired StudentVerificationRepository verificationRepository;
    @Autowired AuditLogRepository auditLogRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    UUID adminId;

    @BeforeEach
    void setUp() {
        var admin = userRepository.save(AdminUser.createAdmin());
        adminId = admin.getId();
        credentialRepository.save(PasswordCredential.create(adminId, ADMIN_EMAIL, passwordEncoder.encode("password")));
        var member = userRepository.save(AdminUser.createUser());
        var reporter = userRepository.save(AdminUser.createUser());
        eventRepository.save(Event.createPending(member.getId()));
        reportRepository.save(Report.create(reporter.getId(), ReportTargetType.HOST, member.getId(), "ABUSE", "신고", null));
        entityManager.flush();

        var universityId = UUID.randomUUID();
        var now = Instant.now();
        jdbcTemplate.update("insert into universities(id, name_ko, active, created_at, updated_at) values (?, ?, true, ?, ?)",
                universityId, "대시보드대학교", now, now);
        verificationRepository.save(StudentVerification.pending(member.getId(), "member@school.ac.kr", universityId, null, null));
        auditLogRepository.save(AuditLog.record(adminId, "DASHBOARD_TEST_ACTION", "USER", member.getId(), Map.of()));
        entityManager.flush();
    }

    @Test
    void dashboardReturnsLiveQueueUserAndActivityCounts() throws Exception {
        mockMvc.perform(get("/api/admin/v1/dashboard").with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.queues.pendingEvents").value(1))
                .andExpect(jsonPath("$.data.queues.unresolvedReports").value(1))
                .andExpect(jsonPath("$.data.queues.pendingVerifications").value(1))
                .andExpect(jsonPath("$.data.users.active").value(2))
                .andExpect(jsonPath("$.data.registrations.length()").value(7))
                .andExpect(jsonPath("$.data.recentActivities[0].action").value("DASHBOARD_TEST_ACTION"));
    }

    @Test
    void dashboardRequiresAdminSessionAndIsDocumented() throws Exception {
        mockMvc.perform(get("/api/admin/v1/dashboard")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/v1/dashboard']").exists());
    }
}
