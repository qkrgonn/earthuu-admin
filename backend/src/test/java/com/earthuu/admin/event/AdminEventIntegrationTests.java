package com.earthuu.admin.event;

import com.earthuu.admin.auth.entity.AdminUser;
import com.earthuu.admin.auth.entity.PasswordCredential;
import com.earthuu.admin.auth.repository.AdminUserRepository;
import com.earthuu.admin.auth.repository.PasswordCredentialRepository;
import com.earthuu.admin.event.entity.Event;
import com.earthuu.admin.event.entity.EventVersion;
import com.earthuu.admin.event.repository.EventRepository;
import com.earthuu.admin.event.repository.EventVersionRepository;
import com.earthuu.admin.event.repository.ModerationActionRepository;
import com.earthuu.admin.global.audit.AuditLogRepository;
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
class AdminEventIntegrationTests {
    private static final String ADMIN_EMAIL = "reviewer@earthuu.test";

    @Autowired MockMvc mockMvc;
    @Autowired AdminUserRepository userRepository;
    @Autowired PasswordCredentialRepository credentialRepository;
    @Autowired EventRepository eventRepository;
    @Autowired EventVersionRepository versionRepository;
    @Autowired ModerationActionRepository actionRepository;
    @Autowired AuditLogRepository auditLogRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    UUID eventId;

    @BeforeEach
    void setUp() {
        var admin = userRepository.save(AdminUser.createAdmin());
        credentialRepository.save(PasswordCredential.create(admin.getId(), ADMIN_EMAIL, passwordEncoder.encode("password")));

        var hostId = UUID.randomUUID();
        var universityId = UUID.randomUUID();
        var now = Instant.now();
        jdbcTemplate.update("insert into users(id, status, role, created_at, updated_at) values (?, 'ACTIVE', 'USER', ?, ?)",
                hostId, now, now);
        jdbcTemplate.update("insert into universities(id, name_ko, name_en, active, created_at, updated_at) values (?, ?, ?, true, ?, ?)",
                universityId, "어스유대학교", "Earthuu University", now, now);
        jdbcTemplate.update("insert into profiles(user_id, name, university_id, created_at, updated_at) values (?, ?, ?, ?, ?)",
                hostId, "김호스트", universityId, now, now);

        var event = eventRepository.saveAndFlush(Event.createPending(hostId));
        var version = versionRepository.saveAndFlush(EventVersion.createSubmitted(
                event.getId(), "한강 플로깅", now.plus(14, ChronoUnit.DAYS),
                now.plus(14, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS), now.minus(1, ChronoUnit.DAYS)));
        event.attachCurrentVersion(version.getId());
        eventRepository.saveAndFlush(event);
        eventId = event.getId();
        entityManager.clear();
    }

    @Test
    void pendingEventsCanBeSearchedAndRead() throws Exception {
        mockMvc.perform(get("/api/admin/v1/events")
                        .with(user(ADMIN_EMAIL).roles("ADMIN"))
                        .queryParam("moderationStatus", "PENDING")
                        .queryParam("query", "한강"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("한강 플로깅"))
                .andExpect(jsonPath("$.data.content[0].hostName").value("김호스트"));

        mockMvc.perform(get("/api/admin/v1/events/{id}", eventId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(eventId.toString()))
                .andExpect(jsonPath("$.data.moderationStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.universityName").value("어스유대학교"));
    }

    @Test
    void nonexistentEventReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/v1/events/{id}", UUID.randomUUID())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"));
    }

    @Test
    void participantsEndpointReturnsAnEmptyListForEventWithoutParticipants() throws Exception {
        mockMvc.perform(get("/api/admin/v1/events/{id}/participants", eventId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void openApiContainsEventReviewPathsAndSecuritySchemes() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/v1/events/{eventId}/review/approve']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.sessionAuth").exists())
                .andExpect(jsonPath("$.components.securitySchemes.csrfToken").exists());
    }

    @Test
    void anonymousAndRegularUsersCannotAccessAdminEvents() throws Exception {
        mockMvc.perform(get("/api/admin/v1/events"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/admin/v1/events").with(user("user@earthuu.test").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void reviewCanStartOnlyOnce() throws Exception {
        startReview();

        mockMvc.perform(post("/api/admin/v1/events/{id}/review/start", eventId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVIEW_ALREADY_STARTED"));
    }

    @Test
    void approvalChangesStatusAndWritesBothHistories() throws Exception {
        startReview();

        mockMvc.perform(post("/api/admin/v1/events/{id}/review/approve", eventId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.moderationStatus").value("APPROVED"));

        entityManager.flush();
        assertThat(actionRepository.findByEventIdOrderByCreatedAtDesc(eventId)).hasSize(2);
        assertThat(auditLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc("EVENT", eventId)).hasSize(2);
    }

    @Test
    void currentAdminCanReadEventsTheyReviewed() throws Exception {
        startReview();
        mockMvc.perform(post("/api/admin/v1/events/{id}/review/approve", eventId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/v1/events/reviewed-by-me")
                        .with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$.data[0].decision").value("APPROVED"))
                .andExpect(jsonPath("$.data[0].reviewedAt").exists());
    }

    @Test
    void eventCannotBeApprovedBeforeReviewStarts() throws Exception {
        mockMvc.perform(post("/api/admin/v1/events/{id}/review/approve", eventId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_REVIEW_STATUS"));
    }

    @Test
    void rejectionRequiresReasonAndWritesHistory() throws Exception {
        startReview();

        mockMvc.perform(post("/api/admin/v1/events/{id}/review/reject", eventId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REJECT_REASON_REQUIRED"));

        mockMvc.perform(post("/api/admin/v1/events/{id}/review/reject", eventId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"안전 계획 보완 필요\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.moderationStatus").value("REJECTED"));

        mockMvc.perform(get("/api/admin/v1/events/{id}", eventId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.moderationHistory[0].action").value("REJECTED"))
                .andExpect(jsonPath("$.data.moderationHistory[0].reason").value("안전 계획 보완 필요"));
    }

    private void startReview() throws Exception {
        mockMvc.perform(post("/api/admin/v1/events/{id}/review/start", eventId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.moderationStatus").value("REVIEWING"));
    }
}
