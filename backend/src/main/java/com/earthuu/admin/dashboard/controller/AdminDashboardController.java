package com.earthuu.admin.dashboard.controller;

import com.earthuu.admin.dashboard.dto.DashboardResponse;
import com.earthuu.admin.dashboard.service.AdminDashboardService;
import com.earthuu.admin.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/dashboard")
@Tag(name = "Admin Dashboard", description = "관리자 대시보드 요약 API")
@SecurityRequirement(name = "sessionAuth")
public class AdminDashboardController {
    private final AdminDashboardService service;

    public AdminDashboardController(AdminDashboardService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "대시보드 요약 조회")
    public ApiResponse<DashboardResponse> getDashboard() {
        return ApiResponse.of(service.getDashboard());
    }
}
