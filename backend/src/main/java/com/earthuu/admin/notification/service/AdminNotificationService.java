package com.earthuu.admin.notification.service;

import com.earthuu.admin.auth.service.AdminAuthService;
import com.earthuu.admin.event.dto.PageResponse;
import com.earthuu.admin.global.exception.BusinessException;
import com.earthuu.admin.global.exception.ErrorCode;
import com.earthuu.admin.notification.dto.*;
import com.earthuu.admin.notification.entity.NotificationSetting;
import com.earthuu.admin.notification.entity.OutboxStatus;
import com.earthuu.admin.notification.repository.NotificationRepository;
import com.earthuu.admin.notification.repository.NotificationSettingRepository;
import com.earthuu.admin.notification.repository.OutboxRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AdminNotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository settingRepository;
    private final OutboxRepository outboxRepository;
    private final AdminAuthService adminAuthService;

    public AdminNotificationService(NotificationRepository notificationRepository,
                                    NotificationSettingRepository settingRepository,
                                    OutboxRepository outboxRepository,
                                    AdminAuthService adminAuthService) {
        this.notificationRepository = notificationRepository;
        this.settingRepository = settingRepository;
        this.outboxRepository = outboxRepository;
        this.adminAuthService = adminAuthService;
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> notifications(int page, int size, Authentication authentication) {
        var result = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentAdminId(authentication),
                PageRequest.of(page, size)).map(NotificationResponse::from);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount(Authentication authentication) {
        return new UnreadCountResponse(notificationRepository.countByUserIdAndReadAtIsNull(currentAdminId(authentication)));
    }

    @Transactional
    public NotificationResponse markRead(UUID id, Authentication authentication) {
        var item = notificationRepository.findByIdAndUserId(id, currentAdminId(authentication))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        item.markRead(Instant.now());
        return NotificationResponse.from(item);
    }

    @Transactional
    public UnreadCountResponse markAllRead(Authentication authentication) {
        var userId = currentAdminId(authentication);
        var now = Instant.now();
        notificationRepository.findByUserIdAndReadAtIsNull(userId).forEach(item -> item.markRead(now));
        return new UnreadCountResponse(0);
    }

    @Transactional
    public NotificationSettingResponse settings(Authentication authentication) {
        var userId = currentAdminId(authentication);
        return NotificationSettingResponse.from(settingRepository.findById(userId)
                .orElseGet(() -> settingRepository.save(NotificationSetting.defaults(userId))));
    }

    @Transactional
    public NotificationSettingResponse updateSettings(NotificationSettingRequest request,
                                                       Authentication authentication) {
        var userId = currentAdminId(authentication);
        var item = settingRepository.findById(userId).orElseGet(() -> NotificationSetting.defaults(userId));
        item.update(request.pushEnabled(), request.chatEnabled());
        return NotificationSettingResponse.from(settingRepository.save(item));
    }

    @Transactional(readOnly = true)
    public PageResponse<OutboxResponse> failedOutbox(int page, int size) {
        return PageResponse.from(outboxRepository.findByStatusOrderByCreatedAtDesc(
                OutboxStatus.FAILED, PageRequest.of(page, size)).map(OutboxResponse::from));
    }

    @Transactional
    public OutboxResponse retryOutbox(UUID id) {
        var item = outboxRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.OUTBOX_NOT_FOUND));
        if (item.getStatus() != OutboxStatus.FAILED) throw new BusinessException(ErrorCode.OUTBOX_NOT_FAILED);
        item.retry(Instant.now());
        return OutboxResponse.from(item);
    }

    private UUID currentAdminId(Authentication authentication) {
        return adminAuthService.getAdminSession(authentication.getName()).id();
    }
}
