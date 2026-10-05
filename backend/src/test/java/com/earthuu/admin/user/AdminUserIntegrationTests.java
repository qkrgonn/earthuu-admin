package com.earthuu.admin.user;

import com.earthuu.admin.auth.entity.AdminUser;
import com.earthuu.admin.auth.entity.PasswordCredential;
import com.earthuu.admin.auth.entity.UserStatus;
import com.earthuu.admin.auth.repository.AdminUserRepository;
import com.earthuu.admin.auth.repository.PasswordCredentialRepository;
import com.earthuu.admin.global.audit.AuditLogRepository;
import com.earthuu.admin.report.entity.Report;
import com.earthuu.admin.report.entity.ReportTargetType;
import com.earthuu.admin.report.entity.UserRestriction;
import com.earthuu.admin.report.repository.ReportRepository;
import com.earthuu.admin.report.repository.UserRestrictionRepository;
import com.earthuu.admin.user.repository.UserStatusActionRepository;
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
class AdminUserIntegrationTests {
    private static final String ADMIN_EMAIL = "user-admin@earthuu.test";

    @Autowired MockMvc mockMvc;
    @Autowired AdminUserRepository userRepository;
    @Autowired PasswordCredentialRepository credentialRepository;
    @Autowired UserStatusActionRepository statusActionRepository;
    @Autowired UserRestrictionRepository restrictionRepository;
    @Autowired ReportRepository reportRepository;
    @Autowired AuditLogRepository auditLogRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    UUID adminId;
    UUID userId;
    UUID reporterId;

    @BeforeEach
    void setUp() {
        var admin = userRepository.save(AdminUser.createAdmin());
        adminId = admin.getId();
        credentialRepository.save(PasswordCredential.create(adminId, ADMIN_EMAIL, passwordEncoder.encode("password")));

        var managedUser = userRepository.save(AdminUser.createUser());
        userId = managedUser.getId();
        credentialRepository.save(PasswordCredential.create(userId, "member@earthuu.test", passwordEncoder.encode("password")));
        reporterId = userRepository.save(AdminUser.createUser()).getId();
        entityManager.flush();

        var universityId = UUID.randomUUID();
        var now = Instant.now();
        jdbcTemplate.update("insert into universities(id, name_ko, name_en, active, created_at, updated_at) values (?, ?, ?, true, ?, ?)",
                universityId, "어스유대학교", "Earthuu University", now, now);
        jdbcTemplate.update("insert into profiles(user_id, name, university_id, created_at, updated_at) values (?, ?, ?, ?, ?)",
                userId, "김어스", universityId, now, now);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void usersCanBeSearchedByNameEmailAndId() throws Exception {
        mockMvc.perform(get("/api/admin/v1/users")
                        .with(user(ADMIN_EMAIL).roles("ADMIN"))
                        .queryParam("query", "김어스"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].email").value("member@earthuu.test"));

        mockMvc.perform(get("/api/admin/v1/users")
                        .with(user(ADMIN_EMAIL).roles("ADMIN"))
                        .queryParam("query", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("김어스"));
    }

    @Test
    void userDetailContainsProfileRestrictionsAndHistory() throws Exception {
        suspend(userId, "운영 정책 위반");

        mockMvc.perform(get("/api/admin/v1/users/{id}", userId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("김어스"))
                .andExpect(jsonPath("$.data.universityName").value("어스유대학교"))
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"))
                .andExpect(jsonPath("$.data.statusHistory[0].action").value("SUSPENDED"));
    }

    @Test
    void anonymousAndRegularUsersCannotAccessUserManagement() throws Exception {
        mockMvc.perform(get("/api/admin/v1/users")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/v1/users").with(user("member").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void suspendAndRestoreWriteHistoryAndAuditLogs() throws Exception {
        suspend(userId, "반복적인 정책 위반");
        mockMvc.perform(post("/api/admin/v1/users/{id}/restore", userId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"소명 확인 완료\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        entityManager.flush();
        entityManager.clear();
        assertThat(userRepository.findById(userId).orElseThrow().getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(statusActionRepository.findByUserIdOrderByCreatedAtDesc(userId)).hasSize(2);
        assertThat(auditLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc("USER", userId))
                .extracting("action").containsExactlyInAnyOrder("USER_SUSPENDED", "USER_RESTORED");
    }

    @Test
    void statusTransitionsAndReasonAreValidated() throws Exception {
        mockMvc.perform(post("/api/admin/v1/users/{id}/suspend", userId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_ACTION_REASON_REQUIRED"));

        mockMvc.perform(post("/api/admin/v1/users/{id}/restore", userId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"잘못된 상태 전이\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_USER_STATUS"));
    }

    @Test
    void administratorAccountCannotBeSuspended() throws Exception {
        mockMvc.perform(post("/api/admin/v1/users/{id}/suspend", adminId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"관리자 보호 확인\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CANNOT_SANCTION_ADMIN"));
    }

    @Test
    void activeRestrictionCanBeListedAndRevoked() throws Exception {
        var report = reportRepository.saveAndFlush(Report.create(
                reporterId, ReportTargetType.HOST, userId, "ABUSE", "활동 제한 대상", null));
        var restriction = restrictionRepository.saveAndFlush(UserRestriction.impose(
                userId, report.getId(), adminId, "30일 제한", Instant.now().plus(30, ChronoUnit.DAYS)));

        mockMvc.perform(get("/api/admin/v1/users/{id}/restrictions", userId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));

        mockMvc.perform(post("/api/admin/v1/users/{userId}/restrictions/{restrictionId}/revoke",
                        userId, restriction.getId())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"이의 신청 인용\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVOKED"))
                .andExpect(jsonPath("$.data.revokeReason").value("이의 신청 인용"));

        entityManager.flush();
        assertThat(auditLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc("USER", userId))
                .extracting("action").contains("USER_RESTRICTION_REVOKED");
    }

    @Test
    void revokedOrExpiredRestrictionCannotBeRevokedAgain() throws Exception {
        var report = reportRepository.saveAndFlush(Report.create(
                reporterId, ReportTargetType.HOST, userId, "ABUSE", "활동 제한 대상", null));
        var restriction = restrictionRepository.saveAndFlush(UserRestriction.impose(
                userId, report.getId(), adminId, "제한", Instant.now().plus(1, ChronoUnit.DAYS)));
        revoke(restriction.getId());
        mockMvc.perform(post("/api/admin/v1/users/{userId}/restrictions/{restrictionId}/revoke",
                        userId, restriction.getId())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"중복 해제\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESTRICTION_NOT_ACTIVE"));
    }

    @Test
    void openApiContainsUserManagementPaths() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/v1/users/{userId}/suspend']").exists())
                .andExpect(jsonPath("$.paths['/api/admin/v1/users/{userId}/restrictions/{restrictionId}/revoke']").exists());
    }

    private void suspend(UUID id, String reason) throws Exception {
        mockMvc.perform(post("/api/admin/v1/users/{id}/suspend", id)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"" + reason + "\"}"))
                .andExpect(status().isOk());
    }

    private void revoke(UUID restrictionId) throws Exception {
        mockMvc.perform(post("/api/admin/v1/users/{userId}/restrictions/{restrictionId}/revoke", userId, restrictionId)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"해제\"}"))
                .andExpect(status().isOk());
    }
}
