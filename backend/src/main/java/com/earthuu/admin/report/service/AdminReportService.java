package com.earthuu.admin.report.service;

import com.earthuu.admin.auth.entity.UserRole;
import com.earthuu.admin.auth.repository.AdminUserRepository;
import com.earthuu.admin.auth.service.AdminAuthService;
import com.earthuu.admin.event.repository.EventRepository;
import com.earthuu.admin.global.audit.AuditLogService;
import com.earthuu.admin.global.exception.BusinessException;
import com.earthuu.admin.global.exception.ErrorCode;
import com.earthuu.admin.report.dto.DispositionHistoryResponse;
import com.earthuu.admin.report.dto.ReportActionResponse;
import com.earthuu.admin.report.dto.ReportDetailResponse;
import com.earthuu.admin.report.dto.ReportListResponse;
import com.earthuu.admin.report.dto.ReportPageResponse;
import com.earthuu.admin.report.dto.ReportSearchRequest;
import com.earthuu.admin.report.dto.ReportSort;
import com.earthuu.admin.report.entity.EventDisposition;
import com.earthuu.admin.report.entity.Report;
import com.earthuu.admin.report.entity.ReportResolution;
import com.earthuu.admin.report.entity.ReportStatus;
import com.earthuu.admin.report.entity.ReportTargetType;
import com.earthuu.admin.report.repository.EventDispositionRepository;
import com.earthuu.admin.report.repository.ReportRepository;
import com.earthuu.admin.report.repository.UserProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminReportService {
    private final ReportRepository reportRepository;
    private final EventDispositionRepository dispositionRepository;
    private final EventRepository eventRepository;
    private final AdminUserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final AdminAuthService adminAuthService;
    private final AuditLogService auditLogService;

    public AdminReportService(ReportRepository reportRepository,
                              EventDispositionRepository dispositionRepository,
                              EventRepository eventRepository,
                              AdminUserRepository userRepository,
                              UserProfileRepository profileRepository,
                              AdminAuthService adminAuthService,
                              AuditLogService auditLogService) {
        this.reportRepository = reportRepository;
        this.dispositionRepository = dispositionRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.adminAuthService = adminAuthService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public ReportPageResponse<ReportListResponse> search(ReportSearchRequest request) {
        validateDateRange(request);
        Sort sort = Sort.by(request.getSort() == ReportSort.OLDEST ? Sort.Direction.ASC : Sort.Direction.DESC, "createdAt");
        var page = reportRepository.findAll(ReportSpecifications.search(request),
                        PageRequest.of(request.getPage(), request.getSize(), sort))
                .map(report -> ReportListResponse.from(report, targetName(report)));
        return ReportPageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public ReportDetailResponse getDetail(UUID reportId) {
        var report = getReport(reportId);
        var history = dispositionRepository.findByReportIdOrderByCreatedAtDesc(reportId).stream()
                .map(DispositionHistoryResponse::from)
                .toList();
        return ReportDetailResponse.from(report, targetName(report), history);
    }

    @Transactional(readOnly = true)
    public List<ReportListResponse> getResolvedByMe(Authentication authentication) {
        var actorId = currentAdminId(authentication);
        return reportRepository.findByResolvedByAndStatusOrderByResolvedAtDesc(actorId, ReportStatus.RESOLVED).stream()
                .map(report -> ReportListResponse.from(report, targetName(report)))
                .toList();
    }

    @Transactional
    public ReportActionResponse startReview(UUID reportId, Authentication authentication) {
        var report = getReport(reportId);
        var actorId = currentAdminId(authentication);
        report.startReview(actorId);
        auditLogService.recordReportAction(actorId, "REPORT_REVIEW_STARTED", reportId,
                Map.of("newStatus", report.getStatus().name()));
        reportRepository.flush();
        return ReportActionResponse.from(report);
    }

    @Transactional
    public ReportActionResponse resolve(UUID reportId, ReportResolution resolution, String reason,
                                        Authentication authentication) {
        if (reason == null || reason.isBlank()) throw new BusinessException(ErrorCode.REPORT_REASON_REQUIRED);
        var report = getReport(reportId);
        var actorId = currentAdminId(authentication);
        UUID eventId = applyTargetAction(report, resolution);
        report.resolve(resolution, reason.trim(), actorId);
        dispositionRepository.save(EventDisposition.record(report, eventId, actorId, resolution, reason.trim()));
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("targetType", report.getTargetType().name());
        metadata.put("targetId", report.getTargetId());
        metadata.put("resolution", resolution.name());
        metadata.put("reason", reason.trim());
        auditLogService.recordReportAction(actorId, "REPORT_RESOLVED", reportId, metadata);
        reportRepository.flush();
        return ReportActionResponse.from(report);
    }

    private UUID applyTargetAction(Report report, ReportResolution resolution) {
        if (!resolution.supports(report.getTargetType())) {
            throw new BusinessException(ErrorCode.INVALID_REPORT_RESOLUTION);
        }
        if (report.getTargetType() == ReportTargetType.EVENT) {
            var event = eventRepository.findById(report.getTargetId()).orElse(null);
            if (requiresTarget(resolution) && event == null) throw new BusinessException(ErrorCode.REPORT_TARGET_NOT_FOUND);
            if (event == null) return null;
            if (resolution == ReportResolution.EVENT_SUSPENDED) event.suspend();
            if (resolution == ReportResolution.EVENT_DISCARDED) event.discard();
            return event.getId();
        }

        if (requiresTarget(resolution)) {
            var user = userRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_TARGET_NOT_FOUND));
            if (user.getRole() == UserRole.ADMIN) throw new BusinessException(ErrorCode.CANNOT_SANCTION_ADMIN);
            if (resolution == ReportResolution.HOST_SUSPENDED) user.suspend();
        }
        return null;
    }

    private boolean requiresTarget(ReportResolution resolution) {
        return switch (resolution) {
            case CONTENT_HIDDEN, EVENT_SUSPENDED, EVENT_DISCARDED, HOST_RESTRICTED, HOST_SUSPENDED -> true;
            default -> false;
        };
    }

    private String targetName(Report report) {
        if (report.getTargetType() == ReportTargetType.EVENT) {
            return eventRepository.findById(report.getTargetId())
                    .map(event -> event.getCurrentVersion() == null ? null : event.getCurrentVersion().getTitle())
                    .orElse(null);
        }
        return profileRepository.findById(report.getTargetId()).map(profile -> profile.getName()).orElse(null);
    }

    private Report getReport(UUID reportId) {
        return reportRepository.findById(reportId).orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));
    }

    private UUID currentAdminId(Authentication authentication) {
        return adminAuthService.getAdminSession(authentication.getName()).id();
    }

    private void validateDateRange(ReportSearchRequest request) {
        if (request.getReportedFrom() != null && request.getReportedTo() != null
                && request.getReportedFrom().isAfter(request.getReportedTo())) {
            throw new BusinessException(ErrorCode.INVALID_REPORT_DATE_RANGE);
        }
    }
}
