package com.earthuu.admin.notification.service;

import com.earthuu.admin.notification.entity.Notification;
import com.earthuu.admin.notification.entity.OutboxMessage;
import com.earthuu.admin.notification.entity.OutboxStatus;
import com.earthuu.admin.notification.repository.NotificationRepository;
import com.earthuu.admin.notification.repository.OutboxRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OutboxProcessor {
    private final OutboxRepository outboxRepository;
    private final NotificationRepository notificationRepository;

    public OutboxProcessor(OutboxRepository outboxRepository, NotificationRepository notificationRepository) {
        this.outboxRepository = outboxRepository;
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay-ms:5000}")
    @Transactional
    public void processDue() {
        var messages = outboxRepository.lockDue(OutboxStatus.PENDING, Instant.now(), PageRequest.of(0, 20));
        for (var message : messages) process(message);
    }

    private void process(OutboxMessage message) {
        message.processing();
        try {
            if (!"IN_APP_NOTIFICATION".equals(message.getTopic())) {
                throw new IllegalArgumentException("지원하지 않는 Outbox topic입니다: " + message.getTopic());
            }
            if (!notificationRepository.existsByDedupeKey(message.getDedupeKey())) {
                var payload = message.getPayload();
                var targetId = payload.get("targetId") == null ? null : UUID.fromString(payload.get("targetId").toString());
                notificationRepository.save(Notification.create(
                        UUID.fromString(payload.get("recipientId").toString()),
                        payload.get("type").toString(), payload.get("targetType").toString(), targetId,
                        payload.get("title").toString(), payload.get("body").toString(), message.getDedupeKey()));
            }
            message.complete();
        } catch (RuntimeException exception) {
            message.fail(exception.getMessage(), Instant.now());
        }
    }
}
