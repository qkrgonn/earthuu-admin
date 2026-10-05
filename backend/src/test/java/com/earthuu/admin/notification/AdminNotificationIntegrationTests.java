package com.earthuu.admin.notification;

import com.earthuu.admin.auth.entity.AdminUser;
import com.earthuu.admin.auth.entity.PasswordCredential;
import com.earthuu.admin.auth.repository.AdminUserRepository;
import com.earthuu.admin.auth.repository.PasswordCredentialRepository;
import com.earthuu.admin.notification.entity.OutboxMessage;
import com.earthuu.admin.notification.entity.OutboxStatus;
import com.earthuu.admin.notification.repository.NotificationRepository;
import com.earthuu.admin.notification.repository.NotificationSettingRepository;
import com.earthuu.admin.notification.repository.OutboxRepository;
import com.earthuu.admin.notification.service.OutboxProcessor;
import com.earthuu.admin.notification.service.OutboxPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.outbox.fixed-delay-ms=3600000")
@AutoConfigureMockMvc
@Transactional
class AdminNotificationIntegrationTests {
    private static final String ADMIN_EMAIL = "notification-admin@earthuu.test";
    @Autowired MockMvc mockMvc;
    @Autowired AdminUserRepository userRepository;
    @Autowired PasswordCredentialRepository credentialRepository;
    @Autowired NotificationRepository notificationRepository;
    @Autowired NotificationSettingRepository settingRepository;
    @Autowired OutboxRepository outboxRepository;
    @Autowired OutboxPublisher publisher;
    @Autowired OutboxProcessor processor;
    @Autowired PasswordEncoder passwordEncoder;

    UUID adminId;

    @BeforeEach
    void setUp() {
        var admin = userRepository.save(AdminUser.createAdmin());
        adminId = admin.getId();
        credentialRepository.save(PasswordCredential.create(adminId, ADMIN_EMAIL, passwordEncoder.encode("password")));
        userRepository.flush();
    }

    @Test
    void outboxCreatesOneIdempotentNotification() {
        var targetId = UUID.randomUUID();
        publisher.enqueueNotification(adminId, "TEST", "EVENT", targetId, "테스트 알림", "본문", "test:one");
        publisher.enqueueNotification(adminId, "TEST", "EVENT", targetId, "테스트 알림", "본문", "test:one");
        outboxRepository.flush();
        assertThat(outboxRepository.count()).isEqualTo(1);

        processor.processDue();
        assertThat(notificationRepository.count()).isEqualTo(1);
        assertThat(outboxRepository.findAll().getFirst().getStatus()).isEqualTo(OutboxStatus.COMPLETED);
    }

    @Test
    void notificationApiListsCountsAndReadsNotifications() throws Exception {
        publisher.enqueueNotification(adminId, "TEST", "USER", adminId, "확인 필요", "알림 본문", "test:api");
        outboxRepository.flush();
        processor.processDue();
        var id = notificationRepository.findAll().getFirst().getId();

        mockMvc.perform(get("/api/admin/v1/notifications").with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.content[0].title").value("확인 필요"));
        mockMvc.perform(get("/api/admin/v1/notifications/unread-count").with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.count").value(1));
        mockMvc.perform(post("/api/admin/v1/notifications/{id}/read", id)
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.readAt").exists());
    }

    @Test
    void notificationSettingsHaveDefaultsAndCanBeUpdated() throws Exception {
        mockMvc.perform(get("/api/admin/v1/notification-settings").with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.pushEnabled").value(true));
        mockMvc.perform(patch("/api/admin/v1/notification-settings")
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pushEnabled\":false,\"chatEnabled\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.pushEnabled").value(false));
        assertThat(settingRepository.findById(adminId)).get().extracting("chatEnabled").isEqualTo(true);
    }

    @Test
    void failedOutboxCanBeListedAndRetried() throws Exception {
        var failed = OutboxMessage.pending("UNKNOWN", UUID.randomUUID(), Map.of("value", "x"), "failed:one");
        for (int i = 0; i < 5; i++) failed.fail("전송 실패", Instant.now());
        outboxRepository.saveAndFlush(failed);

        mockMvc.perform(get("/api/admin/v1/outbox").with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.content[0].status").value("FAILED"));
        mockMvc.perform(post("/api/admin/v1/outbox/{id}/retry", failed.getId())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void notificationEndpointsRequireAuthenticationAndAreDocumented() throws Exception {
        mockMvc.perform(get("/api/admin/v1/notifications")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/v1/notifications']").exists())
                .andExpect(jsonPath("$.paths['/api/admin/v1/outbox/{outboxId}/retry']").exists());
    }
}
