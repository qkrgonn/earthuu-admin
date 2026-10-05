package com.earthuu.admin.event.service;

import com.earthuu.admin.auth.service.AdminAuthService;
import com.earthuu.admin.event.dto.EventDetailResponse;
import com.earthuu.admin.event.dto.EventListResponse;
import com.earthuu.admin.event.dto.EventParticipantResponse;
import com.earthuu.admin.event.dto.EventReviewResponse;
import com.earthuu.admin.event.dto.EventSearchRequest;
import com.earthuu.admin.event.dto.ModerationHistoryResponse;
import com.earthuu.admin.event.dto.PageResponse;
import com.earthuu.admin.event.dto.ReviewedEventResponse;
import com.earthuu.admin.event.entity.Event;
import com.earthuu.admin.event.entity.ModerationAction;
import com.earthuu.admin.event.entity.ModerationActionType;
import com.earthuu.admin.event.repository.EventParticipationRepository;
import com.earthuu.admin.event.repository.EventRepository;
import com.earthuu.admin.event.repository.ModerationActionRepository;
import com.earthuu.admin.global.audit.AuditLogService;
import com.earthuu.admin.global.exception.BusinessException;
import com.earthuu.admin.global.exception.ErrorCode;
import com.earthuu.admin.notification.service.OutboxPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class AdminEventService {
    private final EventRepository eventRepository;
    private final EventParticipationRepository participationRepository;
    private final ModerationActionRepository moderationActionRepository;
    private final AdminAuthService adminAuthService;
    private final AuditLogService auditLogService;
    private final OutboxPublisher outboxPublisher;

    public AdminEventService(
            EventRepository eventRepository,
            EventParticipationRepository participationRepository,
            ModerationActionRepository moderationActionRepository,
            AdminAuthService adminAuthService,
            AuditLogService auditLogService,
            OutboxPublisher outboxPublisher
    ) {
        this.eventRepository = eventRepository;
        this.participationRepository = participationRepository;
        this.moderationActionRepository = moderationActionRepository;
        this.adminAuthService = adminAuthService;
        this.auditLogService = auditLogService;
        this.outboxPublisher = outboxPublisher;
    }

    @Transactional(readOnly = true)
    public PageResponse<EventListResponse> search(EventSearchRequest request) {
        validateDateRange(request);
        var page = eventRepository.findAll(EventSpecifications.search(request), PageRequest.of(request.getPage(), request.getSize()))
                .map(EventListResponse::from);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getDetail(UUID eventId) {
        var event = getEvent(eventId);
        requireCurrentVersion(event);
        var history = moderationActionRepository.findByEventIdOrderByCreatedAtDesc(eventId).stream()
                .map(ModerationHistoryResponse::from)
                .toList();
        return EventDetailResponse.from(event, history);
    }

    @Transactional(readOnly = true)
    public List<EventParticipantResponse> getParticipants(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new BusinessException(ErrorCode.EVENT_NOT_FOUND);
        }
        return participationRepository.findByEventIdOrderByConfirmedAtAsc(eventId).stream()
                .map(EventParticipantResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewedEventResponse> getMyReviewedEvents(Authentication authentication) {
        var actorId = currentAdminId(authentication);
        var actions = moderationActionRepository.findByActorIdAndActionInOrderByCreatedAtDesc(
                actorId, List.of(ModerationActionType.APPROVED, ModerationActionType.REJECTED));
        var latestActions = actions.stream().collect(Collectors.toMap(
                ModerationAction::getEventId,
                Function.identity(),
                (first, ignored) -> first,
                LinkedHashMap::new));
        var eventsById = eventRepository.findAllById(latestActions.keySet()).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));
        return latestActions.values().stream()
                .filter(action -> eventsById.containsKey(action.getEventId()))
                .map(action -> ReviewedEventResponse.from(eventsById.get(action.getEventId()), action))
                .toList();
    }

    @Transactional
    public EventReviewResponse startReview(UUID eventId, Authentication authentication) {
        var event = getEvent(eventId);
        var actorId = currentAdminId(authentication);
        var previous = event.getModerationStatus().name();
        event.startReview();
        record(event, actorId, ModerationActionType.REVIEW_STARTED, null, previous);
        eventRepository.flush();
        return EventReviewResponse.from(event);
    }

    @Transactional
    public EventReviewResponse approve(UUID eventId, Authentication authentication) {
        var event = getEvent(eventId);
        var actorId = currentAdminId(authentication);
        var previous = event.getModerationStatus().name();
        event.approve();
        record(event, actorId, ModerationActionType.APPROVED, "승인", previous);
        outboxPublisher.enqueueNotification(event.getHostId(), "EVENT_APPROVED", "EVENT", eventId,
                "이벤트 심사가 승인되었습니다.", "등록한 이벤트가 승인되었습니다.", "event-approved:" + eventId);
        eventRepository.flush();
        return EventReviewResponse.from(event);
    }

    @Transactional
    public EventReviewResponse reject(UUID eventId, String reason, Authentication authentication) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.REJECT_REASON_REQUIRED);
        }
        var event = getEvent(eventId);
        var actorId = currentAdminId(authentication);
        var previous = event.getModerationStatus().name();
        event.reject();
        record(event, actorId, ModerationActionType.REJECTED, reason.trim(), previous);
        outboxPublisher.enqueueNotification(event.getHostId(), "EVENT_REJECTED", "EVENT", eventId,
                "이벤트 심사가 반려되었습니다.", "이벤트 심사 결과와 반려 사유를 확인해 주세요.",
                "event-rejected:" + eventId);
        eventRepository.flush();
        return EventReviewResponse.from(event);
    }

    private void record(Event event, UUID actorId, ModerationActionType action, String reason, String previousStatus) {
        requireCurrentVersion(event);
        moderationActionRepository.save(ModerationAction.record(
                event.getId(), event.getCurrentVersionId(), actorId, action, reason));
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("previousStatus", previousStatus);
        metadata.put("newStatus", event.getModerationStatus().name());
        if (reason != null) {
            metadata.put("reason", reason);
        }
        auditLogService.recordEventAction(actorId, "EVENT_REVIEW_" + action.name(), event.getId(), metadata);
    }

    private Event getEvent(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
    }

    private void requireCurrentVersion(Event event) {
        if (event.getCurrentVersionId() == null || event.getCurrentVersion() == null) {
            throw new BusinessException(ErrorCode.EVENT_VERSION_NOT_FOUND);
        }
    }

    private UUID currentAdminId(Authentication authentication) {
        return adminAuthService.getAdminSession(authentication.getName()).id();
    }

    private void validateDateRange(EventSearchRequest request) {
        if (request.getSubmittedFrom() != null && request.getSubmittedTo() != null
                && request.getSubmittedFrom().isAfter(request.getSubmittedTo())) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }
    }
}
