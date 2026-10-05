package com.earthuu.admin.report.controller;

import com.earthuu.admin.global.response.ApiResponse;
import com.earthuu.admin.report.dto.ReportActionResponse;
import com.earthuu.admin.report.dto.ReportDetailResponse;
import com.earthuu.admin.report.dto.ReportListResponse;
import com.earthuu.admin.report.dto.ReportPageResponse;
import com.earthuu.admin.report.dto.ReportResolveRequest;
import com.earthuu.admin.report.dto.ReportSearchRequest;
import com.earthuu.admin.report.service.AdminReportService;
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
@RequestMapping("/api/admin/v1/reports")
@Tag(name = "Admin Report Management", description = "관리자 신고 조회 및 처리 API")
@SecurityRequirement(name = "sessionAuth")
public class AdminReportController {
    private final AdminReportService service;

    public AdminReportController(AdminReportService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "신고 목록 조회")
    public ApiResponse<ReportPageResponse<ReportListResponse>> search(@Valid @ParameterObject ReportSearchRequest request) {
        return ApiResponse.of(service.search(request));
    }

    @GetMapping("/resolved-by-me")
    @Operation(summary = "내가 처리한 신고 조회")
    public ApiResponse<List<ReportListResponse>> getResolvedByMe(Authentication authentication) {
        return ApiResponse.of(service.getResolvedByMe(authentication));
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "신고 상세 및 처리 이력 조회")
    public ApiResponse<ReportDetailResponse> getDetail(@PathVariable UUID reportId) {
        return ApiResponse.of(service.getDetail(reportId));
    }

    @PostMapping("/{reportId}/review/start")
    @Operation(summary = "신고 검토 시작")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<ReportActionResponse> startReview(@PathVariable UUID reportId, Authentication authentication) {
        return ApiResponse.of(service.startReview(reportId, authentication));
    }

    @PostMapping("/{reportId}/resolve")
    @Operation(summary = "신고 처리 완료")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<ReportActionResponse> resolve(@PathVariable UUID reportId,
                                                     @Valid @RequestBody ReportResolveRequest request,
                                                     Authentication authentication) {
        return ApiResponse.of(service.resolve(reportId, request.resolution(), request.reason(),
                request.restrictionEndsAt(), authentication));
    }
}
