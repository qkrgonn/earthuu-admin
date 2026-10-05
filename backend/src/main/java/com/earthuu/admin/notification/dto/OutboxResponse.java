package com.earthuu.admin.notification.dto;

import com.earthuu.admin.notification.entity.OutboxMessage;
import java.time.Instant;
import java.util.UUID;

public record OutboxResponse(UUID id, String topic, UUID aggregateId, String dedupeKey,
                             String status, int attempts, Instant nextAttemptAt,
                             String lastError, Instant createdAt) {
    public static OutboxResponse from(OutboxMessage item) {
        return new OutboxResponse(item.getId(), item.getTopic(), item.getAggregateId(), item.getDedupeKey(),
                item.getStatus().name(), item.getAttempts(), item.getNextAttemptAt(),
                item.getLastError(), item.getCreatedAt());
    }
}
