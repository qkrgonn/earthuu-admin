package com.earthuu.admin.report;

import com.earthuu.admin.auth.entity.AdminUser;
import com.earthuu.admin.auth.entity.PasswordCredential;
import com.earthuu.admin.auth.repository.AdminUserRepository;
import com.earthuu.admin.auth.repository.PasswordCredentialRepository;
import com.earthuu.admin.event.entity.Event;
import com.earthuu.admin.event.entity.EventVersion;
import com.earthuu.admin.event.entity.LifecycleStatus;
import com.earthuu.admin.event.repository.EventRepository;
import com.earthuu.admin.event.repository.EventVersionRepository;
import com.earthuu.admin.global.audit.AuditLogRepository;
import com.earthuu.admin.report.entity.Report;
import com.earthuu.admin.report.entity.ReportStatus;
import com.earthuu.admin.report.entity.ReportTargetType;
import com.earthuu.admin.report.repository.EventDispositionRepository;
import com.earthuu.admin.report.repository.ReportRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminReportIntegrationTests {
    private static final String ADMIN_EMAIL = "reporter-admin@earthuu.test";

    @Autowired MockMvc mockMvc;
    @Autowired AdminUserRepository userRepository;
    @Autowired PasswordCredentialRepository credentialRepository;
    @Autowired ReportRepository reportRepository;
    @Autowired EventDispositionRepository dispositionRepository;
    @Autowired EventRepository eventRepository;
    @Autowired EventVersionRepository versionRepository;
    @Autowired AuditLogRepository auditLogRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    UUID adminId;
    UUID reporterId;
    UUID hostId;
    UUID eventId;
    UUID eventReportId;

    @BeforeEach
    void setUp() {
        var admin = userRepository.save(AdminUser.createAdmin());
        adminId = admin.getId();
        credentialRepository.save(PasswordCredential.create(adminId, ADMIN_EMAIL, passwordEncoder.encode("password")));

        reporterId = createUser("USER");
        hostId = createUser("USER");
        var universityId = UUID.randomUUID();
        var now = Instant.now();
        jdbcTemplate.update("insert into universities(id, name_ko, name_en, active, created_at, updated_at) values (?, ?, ?, true, ?, ?)",
                universityId, "어스유대학교", "Earthuu University", now, now);
        jdbcTemplate.update("insert into profiles(user_id, name, university_id, created_at, updated_at) values (?, ?, ?, ?, ?)",
                hostId, "신고 대상 호스트", universityId, now, now);

        var event = eventRepository.saveAndFlush(Event.createPending(hostId));
        var version = versionRepository.saveAndFlush(EventVersion.createSubmitted(event.getId(), "문제 이벤트",
                now.plus(7, ChronoUnit.DAYS), now.plus(7, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS), now));
        event.attachCurrentVersion(version.getId());
        eventRepository.saveAndFlush(event);
        eventId = event.getId();

        eventReportId = reportRepository.saveAndFlush(Report.create(reporterId, ReportTargetType.EVENT, eventId,
                "UNSAFE_CONTENT", "안전하지 않은 내용이 포함되어 있습니다.", "https://evidence.test/1")).getId();
        entityManager.clear();
    }

    @Test
    void reportsCanBeSearchedAndRead() throws Exception {
        mockMvc.perform(get("/api/admin/v1/reports")
                        .with(user(ADMIN_EMAIL).roles("ADMIN"))
                        .queryParam("status", "RECEIVED")
                        .queryParam("query", "문제"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].targetName").value("문제 이벤트"));

        mockMvc.perform(get("/api/admin/v1/reports/{id}", eventReportId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(eventReportId.toString()))
                .andExpect(jsonPath("$.data.status").value("RECEIVED"))
                .andExpect(jsonPath("$.data.history").isEmpty());
    }

    @Test
    void anonymousAndRegularUsersCannotAccessReports() throws Exception {
        mockMvc.perform(get("/api/admin/v1/reports"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/v1/reports").with(user("user@earthuu.test").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void reviewCanStartOnlyOnce() throws Exception {
        startReview(eventReportId);

        mockMvc.perform(post("/api/admin/v1/reports/{id}/review/start", eventReportId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_REPORT_STATUS"));
    }

    @Test
    void resolutionRequiresReasonAndInvestigatingStatus() throws Exception {
        mockMvc.perform(post("/api/admin/v1/reports/{id}/resolve", eventReportId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"NO_ACTION\",\"reason\":\"확인 완료\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_REPORT_STATUS"));

        startReview(eventReportId);
        mockMvc.perform(post("/api/admin/v1/reports/{id}/resolve", eventReportId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"NO_ACTION\",\"reason\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REPORT_REASON_REQUIRED"));
    }

    @Test
    void eventSuspensionUpdatesTargetAndWritesHistories() throws Exception {
        startReview(eventReportId);

        mockMvc.perform(post("/api/admin/v1/reports/{id}/resolve", eventReportId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"EVENT_SUSPENDED\",\"reason\":\"안전 문제 확인\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolution").value("EVENT_SUSPENDED"));

        entityManager.flush();
        entityManager.clear();
        assertThat(eventRepository.findById(eventId).orElseThrow().getLifecycleStatus()).isEqualTo(LifecycleStatus.SUSPENDED);
        assertThat(dispositionRepository.findByReportIdOrderByCreatedAtDesc(eventReportId)).hasSize(1);
        assertThat(auditLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc("REPORT", eventReportId)).hasSize(2);
    }

    @Test
    void eventReportRejectsHostOnlyResolution() throws Exception {
        startReview(eventReportId);

        mockMvc.perform(post("/api/admin/v1/reports/{id}/resolve", eventReportId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"HOST_SUSPENDED\",\"reason\":\"잘못된 조치\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REPORT_RESOLUTION"));
    }

    @Test
    void hostSuspensionChangesUserStatus() throws Exception {
        var hostReportId = reportRepository.saveAndFlush(Report.create(reporterId, ReportTargetType.HOST, hostId,
                "ABUSE", "반복적인 운영 정책 위반", null)).getId();
        startReview(hostReportId);

        mockMvc.perform(post("/api/admin/v1/reports/{id}/resolve", hostReportId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"HOST_SUSPENDED\",\"reason\":\"반복 위반\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resolution").value("HOST_SUSPENDED"));

        entityManager.flush();
        entityManager.clear();
        assertThat(jdbcTemplate.queryForObject("select status from users where id = ?", String.class, hostId))
                .isEqualTo("SUSPENDED");
    }

    @Test
    void administratorCannotBeSanctioned() throws Exception {
        var adminReportId = reportRepository.saveAndFlush(Report.create(reporterId, ReportTargetType.HOST, adminId,
                "ABUSE", "관리자 대상 신고", null)).getId();
        startReview(adminReportId);

        mockMvc.perform(post("/api/admin/v1/reports/{id}/resolve", adminReportId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"HOST_SUSPENDED\",\"reason\":\"제재 시도\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CANNOT_SANCTION_ADMIN"));
    }

    @Test
    void currentAdminCanReadTheirResolvedReports() throws Exception {
        startReview(eventReportId);
        resolveNoAction(eventReportId);

        mockMvc.perform(get("/api/admin/v1/reports/resolved-by-me")
                        .with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(eventReportId.toString()))
                .andExpect(jsonPath("$.data[0].resolution").value("NO_ACTION"));
    }

    @Test
    void openApiContainsReportPaths() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/v1/reports/{reportId}/resolve']").exists());
    }

    private UUID createUser(String role) {
        var id = UUID.randomUUID();
        var now = Instant.now();
        jdbcTemplate.update("insert into users(id, status, role, created_at, updated_at) values (?, 'ACTIVE', ?, ?, ?)",
                id, role, now, now);
        return id;
    }

    private void startReview(UUID reportId) throws Exception {
        mockMvc.perform(post("/api/admin/v1/reports/{id}/review/start", reportId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INVESTIGATING"));
    }

    private void resolveNoAction(UUID reportId) throws Exception {
        mockMvc.perform(post("/api/admin/v1/reports/{id}/resolve", reportId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"NO_ACTION\",\"reason\":\"신고 내용 확인 결과 문제 없음\"}"))
                .andExpect(status().isOk());
    }
}
