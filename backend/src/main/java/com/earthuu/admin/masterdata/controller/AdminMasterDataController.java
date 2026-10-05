package com.earthuu.admin.masterdata.controller;

import com.earthuu.admin.global.response.ApiResponse;
import com.earthuu.admin.masterdata.dto.*;
import com.earthuu.admin.masterdata.service.AdminMasterDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/v1/master-data")
@Tag(name = "Master Data Management", description = "대학·학교 도메인·지역·카테고리 기준정보 관리 API")
@SecurityRequirement(name = "sessionAuth")
public class AdminMasterDataController {
    private final AdminMasterDataService service;

    public AdminMasterDataController(AdminMasterDataService service) { this.service = service; }

    @GetMapping("/universities")
    @Operation(summary = "대학 목록 조회")
    public ApiResponse<List<UniversityResponse>> universities(@RequestParam(required = false) String query,
                                                              @RequestParam(required = false) Boolean active) {
        return ApiResponse.of(service.universities(query, active));
    }

    @PostMapping("/universities")
    @SecurityRequirement(name = "csrfToken")
    @Operation(summary = "대학 추가")
    public ApiResponse<UniversityResponse> createUniversity(@Valid @RequestBody UniversityRequest request,
                                                            Authentication authentication) {
        return ApiResponse.of(service.createUniversity(request, authentication));
    }

    @PatchMapping("/universities/{id}")
    @SecurityRequirement(name = "csrfToken")
    @Operation(summary = "대학 수정")
    public ApiResponse<UniversityResponse> updateUniversity(@PathVariable UUID id,
                                                            @Valid @RequestBody UniversityRequest request,
                                                            Authentication authentication) {
        return ApiResponse.of(service.updateUniversity(id, request, authentication));
    }

    @PostMapping("/universities/{id}/activate")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<UniversityResponse> activateUniversity(@PathVariable UUID id, Authentication authentication) {
        return ApiResponse.of(service.setUniversityActive(id, true, authentication));
    }

    @PostMapping("/universities/{id}/deactivate")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<UniversityResponse> deactivateUniversity(@PathVariable UUID id, Authentication authentication) {
        return ApiResponse.of(service.setUniversityActive(id, false, authentication));
    }

    @GetMapping("/universities/{id}/domains")
    @Operation(summary = "대학 이메일 도메인 목록 조회")
    public ApiResponse<List<DomainResponse>> domains(@PathVariable UUID id) {
        return ApiResponse.of(service.domains(id));
    }

    @PostMapping("/universities/{id}/domains")
    @SecurityRequirement(name = "csrfToken")
    @Operation(summary = "대학 이메일 도메인 추가")
    public ApiResponse<DomainResponse> addDomain(@PathVariable UUID id, @Valid @RequestBody DomainRequest request,
                                                Authentication authentication) {
        return ApiResponse.of(service.addDomain(id, request, authentication));
    }

    @PostMapping("/universities/{id}/domains/{domain}/activate")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<DomainResponse> activateDomain(@PathVariable UUID id, @PathVariable String domain,
                                                      Authentication authentication) {
        return ApiResponse.of(service.setDomainActive(id, domain, true, authentication));
    }

    @DeleteMapping("/universities/{id}/domains/{domain}")
    @SecurityRequirement(name = "csrfToken")
    @Operation(summary = "대학 이메일 도메인 비활성화")
    public ApiResponse<DomainResponse> deactivateDomain(@PathVariable UUID id, @PathVariable String domain,
                                                        Authentication authentication) {
        return ApiResponse.of(service.setDomainActive(id, domain, false, authentication));
    }

    @GetMapping("/cities")
    public ApiResponse<List<CityResponse>> cities(@RequestParam(required = false) Boolean active) {
        return ApiResponse.of(service.cities(active));
    }

    @PostMapping("/cities")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<CityResponse> createCity(@Valid @RequestBody CityCreateRequest request,
                                                Authentication authentication) {
        return ApiResponse.of(service.createCity(request, authentication));
    }

    @PatchMapping("/cities/{code}")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<CityResponse> updateCity(@PathVariable String code,
                                                @Valid @RequestBody CityUpdateRequest request,
                                                Authentication authentication) {
        return ApiResponse.of(service.updateCity(code, request, authentication));
    }

    @PostMapping("/cities/{code}/activate")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<CityResponse> activateCity(@PathVariable String code, Authentication authentication) {
        return ApiResponse.of(service.setCityActive(code, true, authentication));
    }

    @PostMapping("/cities/{code}/deactivate")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<CityResponse> deactivateCity(@PathVariable String code, Authentication authentication) {
        return ApiResponse.of(service.setCityActive(code, false, authentication));
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> categories(@RequestParam(required = false) Boolean active) {
        return ApiResponse.of(service.categories(active));
    }

    @PostMapping("/categories")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request,
                                                        Authentication authentication) {
        return ApiResponse.of(service.createCategory(request, authentication));
    }

    @PatchMapping("/categories/{id}")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable UUID id,
                                                        @Valid @RequestBody CategoryUpdateRequest request,
                                                        Authentication authentication) {
        return ApiResponse.of(service.updateCategory(id, request, authentication));
    }

    @PostMapping("/categories/{id}/activate")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<CategoryResponse> activateCategory(@PathVariable UUID id, Authentication authentication) {
        return ApiResponse.of(service.setCategoryActive(id, true, authentication));
    }

    @PostMapping("/categories/{id}/deactivate")
    @SecurityRequirement(name = "csrfToken")
    public ApiResponse<CategoryResponse> deactivateCategory(@PathVariable UUID id, Authentication authentication) {
        return ApiResponse.of(service.setCategoryActive(id, false, authentication));
    }
}
