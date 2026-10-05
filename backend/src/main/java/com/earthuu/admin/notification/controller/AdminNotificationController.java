package com.earthuu.admin.notification.controller;

import com.earthuu.admin.event.dto.PageResponse;
import com.earthuu.admin.global.response.ApiResponse;
import com.earthuu.admin.notification.dto.*;
import com.earthuu.admin.notification.service.AdminNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/admin/v1")
@Tag(name = "Notifications and Outbox", description = "인앱 알림·설정 및 실패 Outbox 관리 API")
@SecurityRequirement(name = "sessionAuth")
public class AdminNotificationController {
    private final AdminNotificationService service;

    public AdminNotificationController(AdminNotificationService service) { this.service = service; }

    @GetMapping("/notifications")
    @Operation(summary = "현재 관리자 알림 목록 조회")
    public ApiResponse<PageResponse<NotificationResponse>> notifications(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            Authentication authentication) {
        return ApiResponse.of(service.notifications(page, size, authentication));
    }

    @GetMapping("/notifications/unread-count")
    public ApiResponse<UnreadCountResponse> unreadCount(Authentication authentication) {
        return ApiResponse.of(service.unreadCount(authentication));
    }

    @PostMapping("/notifications/{notificationId}/read")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<NotificationResponse> markRead(@PathVariable UUID notificationId,
                                                      Authentication authentication) {
        return ApiResponse.of(service.markRead(notificationId, authentication));
    }

    @PostMapping("/notifications/read-all")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<UnreadCountResponse> markAllRead(Authentication authentication) {
        return ApiResponse.of(service.markAllRead(authentication));
    }

    @GetMapping("/notification-settings")
    public ApiResponse<NotificationSettingResponse> settings(Authentication authentication) {
        return ApiResponse.of(service.settings(authentication));
    }

    @PatchMapping("/notification-settings")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<NotificationSettingResponse> updateSettings(
            @Valid @RequestBody NotificationSettingRequest request, Authentication authentication) {
        return ApiResponse.of(service.updateSettings(request, authentication));
    }

    @GetMapping("/outbox")
    @Operation(summary = "실패 Outbox 목록 조회")
    public ApiResponse<PageResponse<OutboxResponse>> failedOutbox(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.of(service.failedOutbox(page, size));
    }

    @PostMapping("/outbox/{outboxId}/retry")
    @SecurityRequirement(name = "csrfToken")
    @Operation(summary = "실패 Outbox 재시도 예약")
    public ApiResponse<OutboxResponse> retryOutbox(@PathVariable UUID outboxId) {
        return ApiResponse.of(service.retryOutbox(outboxId));
    }
}
