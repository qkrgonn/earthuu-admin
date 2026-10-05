package com.earthuu.admin.notification.service;

import com.earthuu.admin.notification.entity.OutboxMessage;
import com.earthuu.admin.notification.repository.OutboxRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

@Service
public class OutboxPublisher {
    private final OutboxRepository repository;

    public OutboxPublisher(OutboxRepository repository) { this.repository = repository; }

    public void enqueueNotification(UUID recipientId, String type, String targetType, UUID targetId,
                                    String title, String body, String dedupeKey) {
        if (repository.existsByDedupeKey(dedupeKey)) return;
        var payload = new HashMap<String, Object>();
        payload.put("recipientId", recipientId.toString());
        payload.put("type", type);
        payload.put("targetType", targetType);
        if (targetId != null) payload.put("targetId", targetId.toString());
        payload.put("title", title);
        payload.put("body", body);
        repository.save(OutboxMessage.pending("IN_APP_NOTIFICATION", targetId, payload, dedupeKey));
    }
}
