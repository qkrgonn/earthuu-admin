package com.earthuu.admin.statistics.controller;

import com.earthuu.admin.global.response.ApiResponse;
import com.earthuu.admin.statistics.dto.*;
import com.earthuu.admin.statistics.service.AdminStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/statistics")
@Tag(name = "Admin Statistics", description = "관리자 기간별 운영 통계 API")
@SecurityRequirement(name = "sessionAuth")
public class AdminStatisticsController {
    private final AdminStatisticsService service;

    public AdminStatisticsController(AdminStatisticsService service) { this.service = service; }

    @GetMapping("/overview")
    @Operation(summary = "기간별 주요 지표 조회")
    public ApiResponse<StatisticsOverviewResponse> overview(@ParameterObject StatisticsQueryRequest request) {
        return ApiResponse.of(service.overview(request));
    }

    @GetMapping("/users")
    @Operation(summary = "사용자 가입 통계 조회")
    public ApiResponse<UserStatisticsResponse> users(@ParameterObject StatisticsQueryRequest request) {
        return ApiResponse.of(service.users(request));
    }

    @GetMapping("/events")
    @Operation(summary = "이벤트 등록·심사 통계 조회")
    public ApiResponse<EventStatisticsResponse> events(@ParameterObject StatisticsQueryRequest request) {
        return ApiResponse.of(service.events(request));
    }

    @GetMapping("/reports")
    @Operation(summary = "신고 접수·처리 통계 조회")
    public ApiResponse<ReportStatisticsResponse> reports(@ParameterObject StatisticsQueryRequest request) {
        return ApiResponse.of(service.reports(request));
    }
}
