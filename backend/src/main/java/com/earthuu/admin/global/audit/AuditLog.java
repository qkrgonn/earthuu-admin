package com.earthuu.admin.global.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "actor_id") private UUID actorId;
    @Column(nullable = false, length = 100) private String action;
    @Column(name = "target_type", nullable = false, length = 50) private String targetType;
    @Column(name = "target_id") private UUID targetId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb") private Map<String, Object> metadata;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected AuditLog() {
    }

    private AuditLog(UUID actorId, String action, String targetType, UUID targetId, Map<String, Object> metadata) {
        this.actorId = actorId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.metadata = metadata;
    }

    public static AuditLog record(UUID actorId, String action, String targetType, UUID targetId, Map<String, Object> metadata) {
        return new AuditLog(actorId, action, targetType, targetId, metadata);
    }

    @PrePersist
    void prePersist() { createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getActorId() { return actorId; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public UUID getTargetId() { return targetId; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Instant getCreatedAt() { return createdAt; }
}
