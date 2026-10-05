package com.earthuu.admin.verification;

import com.earthuu.admin.auth.entity.AdminUser;
import com.earthuu.admin.auth.entity.PasswordCredential;
import com.earthuu.admin.auth.repository.AdminUserRepository;
import com.earthuu.admin.auth.repository.PasswordCredentialRepository;
import com.earthuu.admin.global.audit.AuditLogRepository;
import com.earthuu.admin.verification.entity.StudentVerification;
import com.earthuu.admin.verification.entity.VerificationStatus;
import com.earthuu.admin.verification.repository.StudentVerificationRepository;
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
class AdminVerificationIntegrationTests {
    private static final String ADMIN_EMAIL = "verification-admin@earthuu.test";

    @Autowired MockMvc mockMvc;
    @Autowired AdminUserRepository userRepository;
    @Autowired PasswordCredentialRepository credentialRepository;
    @Autowired StudentVerificationRepository verificationRepository;
    @Autowired AuditLogRepository auditLogRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;

    UUID adminId;
    UUID memberId;
    UUID universityId;
    StudentVerification pending;

    @BeforeEach
    void setUp() {
        var admin = userRepository.save(AdminUser.createAdmin());
        adminId = admin.getId();
        credentialRepository.save(PasswordCredential.create(adminId, ADMIN_EMAIL, passwordEncoder.encode("password")));

        var member = userRepository.save(AdminUser.createUser());
        memberId = member.getId();
        credentialRepository.save(PasswordCredential.create(memberId, "student@earthuu.test", passwordEncoder.encode("password")));
        entityManager.flush();

        universityId = UUID.randomUUID();
        var now = Instant.now();
        jdbcTemplate.update("insert into universities(id, name_ko, name_en, active, created_at, updated_at) values (?, ?, ?, true, ?, ?)",
                universityId, "어스유대학교", "Earthuu University", now, now);
        jdbcTemplate.update("insert into university_domains(domain, university_id, verified_by, verified_at, active, created_at, updated_at) values (?, ?, ?, ?, true, ?, ?)",
                "earthuu.ac.kr", universityId, adminId, now, now, now);
        jdbcTemplate.update("insert into profiles(user_id, name, university_id, created_at, updated_at) values (?, ?, ?, ?, ?)",
                memberId, "김학생", universityId, now, now);

        pending = verificationRepository.saveAndFlush(StudentVerification.pending(
                memberId, "student@earthuu.ac.kr", universityId, now, adminId));
        entityManager.clear();
    }

    @Test
    void verificationsCanBeSearchedAndDetailed() throws Exception {
        mockMvc.perform(get("/api/admin/v1/verifications")
                        .with(user(ADMIN_EMAIL).roles("ADMIN"))
                        .queryParam("query", "김학생")
                        .queryParam("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].schoolEmail").value("student@earthuu.ac.kr"))
                .andExpect(jsonPath("$.data.content[0].universityName").value("어스유대학교"));

        mockMvc.perform(get("/api/admin/v1/verifications/{id}", pending.getId())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("김학생"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void approveWritesStateAndAuditLog() throws Exception {
        mockMvc.perform(post("/api/admin/v1/verifications/{id}/approve", pending.getId())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.reviewedBy").value(adminId.toString()));

        entityManager.flush();
        entityManager.clear();
        assertThat(verificationRepository.findById(pending.getId()).orElseThrow().getStatus())
                .isEqualTo(VerificationStatus.APPROVED);
        assertThat(auditLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
                "STUDENT_VERIFICATION", pending.getId()))
                .extracting("action").containsExactly("STUDENT_VERIFICATION_APPROVED");
    }

    @Test
    void rejectRequiresReasonAndWritesReason() throws Exception {
        mockMvc.perform(post("/api/admin/v1/verifications/{id}/reject", pending.getId())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VERIFICATION_REJECT_REASON_REQUIRED"));

        mockMvc.perform(post("/api/admin/v1/verifications/{id}/reject", pending.getId())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"학생 정보가 일치하지 않음\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectionReason").value("학생 정보가 일치하지 않음"));
    }

    @Test
    void completedVerificationCannotBeProcessedAgain() throws Exception {
        approve(pending.getId());
        mockMvc.perform(post("/api/admin/v1/verifications/{id}/approve", pending.getId())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_VERIFICATION_STATUS"));
    }

    @Test
    void unverifiedEmailCannotBeApproved() throws Exception {
        var unverified = verificationRepository.saveAndFlush(StudentVerification.pending(
                memberId, "other@earthuu.ac.kr", universityId, null, adminId));
        mockMvc.perform(post("/api/admin/v1/verifications/{id}/approve", unverified.getId())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("VERIFICATION_EMAIL_NOT_VERIFIED"));
    }

    @Test
    void invalidDateRangeAndMissingVerificationReturnDomainErrors() throws Exception {
        mockMvc.perform(get("/api/admin/v1/verifications")
                        .with(user(ADMIN_EMAIL).roles("ADMIN"))
                        .queryParam("submittedFrom", "2026-10-05")
                        .queryParam("submittedTo", "2026-10-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_VERIFICATION_DATE_RANGE"));

        mockMvc.perform(get("/api/admin/v1/verifications/{id}", UUID.randomUUID())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VERIFICATION_NOT_FOUND"));
    }

    @Test
    void anonymousAndRegularUsersCannotAccessVerificationManagement() throws Exception {
        mockMvc.perform(get("/api/admin/v1/verifications")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/v1/verifications").with(user("member").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void openApiContainsVerificationPaths() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/v1/verifications/{verificationId}/approve']").exists())
                .andExpect(jsonPath("$.paths['/api/admin/v1/verifications/{verificationId}/reject']").exists());
    }

    private void approve(UUID id) throws Exception {
        mockMvc.perform(post("/api/admin/v1/verifications/{id}/approve", id)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk());
    }
}
