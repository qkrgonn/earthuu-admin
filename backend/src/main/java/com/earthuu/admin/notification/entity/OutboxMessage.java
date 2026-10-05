package com.earthuu.admin.notification.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "outbox")
public class OutboxMessage {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, length = 100) private String topic;
    @Column(name = "aggregate_id") private UUID aggregateId;
    @JdbcTypeCode(SqlTypes.JSON) @Column(nullable = false, columnDefinition = "jsonb") private Map<String, Object> payload;
    @Column(name = "dedupe_key", nullable = false, unique = true, length = 255) private String dedupeKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private OutboxStatus status;
    @Column(nullable = false) private int attempts;
    @Column(name = "next_attempt_at", nullable = false) private Instant nextAttemptAt;
    @Column(name = "last_error", columnDefinition = "text") private String lastError;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected OutboxMessage() {}
    private OutboxMessage(String topic, UUID aggregateId, Map<String, Object> payload, String dedupeKey) {
        this.topic = topic; this.aggregateId = aggregateId; this.payload = payload; this.dedupeKey = dedupeKey;
        this.status = OutboxStatus.PENDING; this.nextAttemptAt = Instant.now();
    }
    public static OutboxMessage pending(String topic, UUID aggregateId, Map<String, Object> payload, String dedupeKey) {
        return new OutboxMessage(topic, aggregateId, payload, dedupeKey);
    }
    public void processing() { status = OutboxStatus.PROCESSING; }
    public void complete() { status = OutboxStatus.COMPLETED; lastError = null; }
    public void fail(String error, Instant now) {
        attempts++;
        lastError = error == null ? "알 수 없는 오류" : error.substring(0, Math.min(error.length(), 1000));
        if (attempts >= 5) status = OutboxStatus.FAILED;
        else { status = OutboxStatus.PENDING; nextAttemptAt = now.plusSeconds(1L << attempts); }
    }
    public void retry(Instant now) { status = OutboxStatus.PENDING; nextAttemptAt = now; lastError = null; }
    @PrePersist void prePersist() { var now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getTopic() { return topic; }
    public UUID getAggregateId() { return aggregateId; }
    public Map<String, Object> getPayload() { return payload; }
    public String getDedupeKey() { return dedupeKey; }
    public OutboxStatus getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public String getLastError() { return lastError; }
    public Instant getCreatedAt() { return createdAt; }
}
