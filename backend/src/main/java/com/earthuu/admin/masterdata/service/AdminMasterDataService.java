package com.earthuu.admin.masterdata.service;

import com.earthuu.admin.auth.service.AdminAuthService;
import com.earthuu.admin.event.entity.University;
import com.earthuu.admin.global.audit.AuditLogService;
import com.earthuu.admin.global.exception.BusinessException;
import com.earthuu.admin.global.exception.ErrorCode;
import com.earthuu.admin.masterdata.dto.*;
import com.earthuu.admin.masterdata.entity.*;
import com.earthuu.admin.masterdata.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminMasterDataService {
    private final MasterUniversityRepository universityRepository;
    private final UniversityDomainRepository domainRepository;
    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;
    private final AdminAuthService adminAuthService;
    private final AuditLogService auditLogService;

    public AdminMasterDataService(MasterUniversityRepository universityRepository,
                                  UniversityDomainRepository domainRepository,
                                  CityRepository cityRepository,
                                  CategoryRepository categoryRepository,
                                  AdminAuthService adminAuthService,
                                  AuditLogService auditLogService) {
        this.universityRepository = universityRepository;
        this.domainRepository = domainRepository;
        this.cityRepository = cityRepository;
        this.categoryRepository = categoryRepository;
        this.adminAuthService = adminAuthService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<UniversityResponse> universities(String query, Boolean active) {
        return universityRepository.findAll().stream()
                .filter(item -> active == null || item.isActive() == active)
                .filter(item -> contains(item.getNameKo(), query) || contains(item.getNameEn(), query))
                .sorted(Comparator.comparing(University::getNameKo))
                .map(UniversityResponse::from).toList();
    }

    @Transactional
    public UniversityResponse createUniversity(UniversityRequest request, Authentication authentication) {
        if (universityRepository.existsByNameKoIgnoreCase(request.nameKo().trim())) duplicate();
        var item = universityRepository.save(University.create(request.nameKo().trim(), trimToNull(request.nameEn())));
        audit(authentication, "UNIVERSITY_CREATED", "UNIVERSITY", item.getId(), Map.of("nameKo", item.getNameKo()));
        return UniversityResponse.from(item);
    }

    @Transactional
    public UniversityResponse updateUniversity(UUID id, UniversityRequest request, Authentication authentication) {
        var item = university(id);
        if (!item.getNameKo().equalsIgnoreCase(request.nameKo().trim())
                && universityRepository.existsByNameKoIgnoreCase(request.nameKo().trim())) duplicate();
        item.update(request.nameKo().trim(), trimToNull(request.nameEn()));
        audit(authentication, "UNIVERSITY_UPDATED", "UNIVERSITY", id, Map.of("nameKo", item.getNameKo()));
        return UniversityResponse.from(item);
    }

    @Transactional
    public UniversityResponse setUniversityActive(UUID id, boolean active, Authentication authentication) {
        var item = university(id);
        if (active) item.activate(); else item.deactivate();
        audit(authentication, active ? "UNIVERSITY_ACTIVATED" : "UNIVERSITY_DEACTIVATED",
                "UNIVERSITY", id, Map.of());
        return UniversityResponse.from(item);
    }

    @Transactional(readOnly = true)
    public List<DomainResponse> domains(UUID universityId) {
        university(universityId);
        return domainRepository.findByUniversityIdOrderByDomain(universityId).stream().map(DomainResponse::from).toList();
    }

    @Transactional
    public DomainResponse addDomain(UUID universityId, DomainRequest request, Authentication authentication) {
        university(universityId);
        var domain = request.domain().trim().toLowerCase();
        if (domainRepository.existsById(domain)) duplicate();
        var actorId = adminAuthService.getAdminSession(authentication.getName()).id();
        var item = domainRepository.save(UniversityDomain.create(universityId, domain, actorId));
        auditLogService.recordMasterDataAction(actorId, "UNIVERSITY_DOMAIN_CREATED", "UNIVERSITY_DOMAIN",
                universityId, Map.of("domain", domain));
        return DomainResponse.from(item);
    }

    @Transactional
    public DomainResponse setDomainActive(UUID universityId, String domain, boolean active,
                                          Authentication authentication) {
        university(universityId);
        var item = domainRepository.findById(domain.toLowerCase())
                .filter(found -> found.getUniversityId().equals(universityId))
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_DATA_NOT_FOUND));
        if (active) item.activate(); else item.deactivate();
        audit(authentication, active ? "UNIVERSITY_DOMAIN_ACTIVATED" : "UNIVERSITY_DOMAIN_DEACTIVATED",
                "UNIVERSITY_DOMAIN", universityId, Map.of("domain", item.getDomain()));
        return DomainResponse.from(item);
    }

    @Transactional(readOnly = true)
    public List<CityResponse> cities(Boolean active) {
        return cityRepository.findAll().stream().filter(item -> active == null || item.isActive() == active)
                .sorted(Comparator.comparing(City::getDisplayNameKo)).map(CityResponse::from).toList();
    }

    @Transactional
    public CityResponse createCity(CityCreateRequest request, Authentication authentication) {
        var code = request.code().trim().toUpperCase();
        if (cityRepository.existsById(code)) duplicate();
        var item = cityRepository.save(City.create(code, request.officialName().trim(),
                request.displayNameKo().trim(), trimToNull(request.displayNameEn())));
        audit(authentication, "CITY_CREATED", "CITY", null, Map.of("code", code));
        return CityResponse.from(item);
    }

    @Transactional
    public CityResponse updateCity(String code, CityUpdateRequest request, Authentication authentication) {
        var item = city(code);
        item.update(request.officialName().trim(), request.displayNameKo().trim(), trimToNull(request.displayNameEn()));
        audit(authentication, "CITY_UPDATED", "CITY", null, Map.of("code", item.getCode()));
        return CityResponse.from(item);
    }

    @Transactional
    public CityResponse setCityActive(String code, boolean active, Authentication authentication) {
        var item = city(code);
        if (active) item.activate(); else item.deactivate();
        audit(authentication, active ? "CITY_ACTIVATED" : "CITY_DEACTIVATED", "CITY", null,
                Map.of("code", item.getCode()));
        return CityResponse.from(item);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> categories(Boolean active) {
        return categoryRepository.findAll().stream().filter(item -> active == null || item.isActive() == active)
                .sorted(Comparator.comparing(Category::getNameKo)).map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request, Authentication authentication) {
        if (categoryRepository.existsByCodeIgnoreCase(request.code().trim())) duplicate();
        var item = categoryRepository.save(Category.create(request.code(), request.nameKo().trim(), trimToNull(request.nameEn())));
        audit(authentication, "CATEGORY_CREATED", "CATEGORY", item.getId(), Map.of("code", item.getCode()));
        return CategoryResponse.from(item);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request, Authentication authentication) {
        var item = category(id);
        item.update(request.nameKo().trim(), trimToNull(request.nameEn()));
        audit(authentication, "CATEGORY_UPDATED", "CATEGORY", id, Map.of("code", item.getCode()));
        return CategoryResponse.from(item);
    }

    @Transactional
    public CategoryResponse setCategoryActive(UUID id, boolean active, Authentication authentication) {
        var item = category(id);
        if (active) item.activate(); else item.deactivate();
        audit(authentication, active ? "CATEGORY_ACTIVATED" : "CATEGORY_DEACTIVATED", "CATEGORY", id,
                Map.of("code", item.getCode()));
        return CategoryResponse.from(item);
    }

    private University university(UUID id) { return universityRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_DATA_NOT_FOUND)); }
    private City city(String code) { return cityRepository.findById(code.toUpperCase())
            .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_DATA_NOT_FOUND)); }
    private Category category(UUID id) { return categoryRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_DATA_NOT_FOUND)); }
    private void duplicate() { throw new BusinessException(ErrorCode.MASTER_DATA_ALREADY_EXISTS); }
    private boolean contains(String value, String query) {
        return query == null || query.isBlank() || value != null && value.toLowerCase().contains(query.trim().toLowerCase());
    }
    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private void audit(Authentication authentication, String action, String type, UUID targetId, Map<String, Object> metadata) {
        var actorId = adminAuthService.getAdminSession(authentication.getName()).id();
        auditLogService.recordMasterDataAction(actorId, action, type, targetId, metadata);
    }
}
