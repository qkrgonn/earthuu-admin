package com.earthuu.admin.event.controller;

import com.earthuu.admin.event.dto.EventDetailResponse;
import com.earthuu.admin.event.dto.EventListResponse;
import com.earthuu.admin.event.dto.EventParticipantResponse;
import com.earthuu.admin.event.dto.EventRejectRequest;
import com.earthuu.admin.event.dto.EventReviewResponse;
import com.earthuu.admin.event.dto.EventSearchRequest;
import com.earthuu.admin.event.dto.PageResponse;
import com.earthuu.admin.event.dto.ReviewedEventResponse;
import com.earthuu.admin.event.service.AdminEventService;
import com.earthuu.admin.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/admin/v1/events")
@Tag(name = "Admin Event Review", description = "관리자 이벤트 조회 및 심사 API")
@SecurityRequirement(name = "sessionAuth")
public class AdminEventController {
    private final AdminEventService service;

    public AdminEventController(AdminEventService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "이벤트 목록 조회", description = "검색, 심사/운영 상태, 신청일, 정렬과 페이지네이션을 지원합니다.")
    public ApiResponse<PageResponse<EventListResponse>> search(@Valid @ParameterObject EventSearchRequest request) {
        return ApiResponse.of(service.search(request));
    }

    @GetMapping("/reviewed-by-me")
    @Operation(summary = "내가 심사한 이벤트 조회", description = "현재 관리자가 승인 또는 반려한 이벤트를 최신 심사순으로 조회합니다.")
    public ApiResponse<List<ReviewedEventResponse>> getMyReviewedEvents(Authentication authentication) {
        return ApiResponse.of(service.getMyReviewedEvents(authentication));
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "이벤트 상세 및 심사 이력 조회")
    public ApiResponse<EventDetailResponse> getDetail(@PathVariable UUID eventId) {
        return ApiResponse.of(service.getDetail(eventId));
    }

    @GetMapping("/{eventId}/participants")
    @Operation(summary = "이벤트 참가자 조회")
    public ApiResponse<List<EventParticipantResponse>> getParticipants(@PathVariable UUID eventId) {
        return ApiResponse.of(service.getParticipants(eventId));
    }

    @PostMapping("/{eventId}/review/start")
    @Operation(summary = "이벤트 심사 시작", description = "PENDING 이벤트를 REVIEWING으로 변경합니다. 세션과 X-XSRF-TOKEN 헤더가 필요합니다.")
    public ApiResponse<EventReviewResponse> startReview(@PathVariable UUID eventId, Authentication authentication) {
        return ApiResponse.of(service.startReview(eventId, authentication));
    }

    @PostMapping("/{eventId}/review/approve")
    @Operation(summary = "이벤트 승인", description = "REVIEWING 이벤트를 APPROVED로 변경합니다. 세션과 X-XSRF-TOKEN 헤더가 필요합니다.")
    public ApiResponse<EventReviewResponse> approve(@PathVariable UUID eventId, Authentication authentication) {
        return ApiResponse.of(service.approve(eventId, authentication));
    }

    @PostMapping("/{eventId}/review/reject")
    @Operation(summary = "이벤트 반려", description = "REVIEWING 이벤트를 사유와 함께 REJECTED로 변경합니다. 세션과 X-XSRF-TOKEN 헤더가 필요합니다.")
    public ApiResponse<EventReviewResponse> reject(
            @PathVariable UUID eventId,
            @Valid @RequestBody EventRejectRequest request,
            Authentication authentication
    ) {
        return ApiResponse.of(service.reject(eventId, request.reason(), authentication));
    }
}
