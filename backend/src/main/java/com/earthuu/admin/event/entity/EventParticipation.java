package com.earthuu.admin.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "participations")
public class EventParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "event_id", nullable = false) private UUID eventId;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private UserProfile profile;
    @Column(name = "quota_group", length = 20) private String quotaGroup;
    @Column(name = "nationality_snapshot", length = 3) private String nationalitySnapshot;
    @Column(nullable = false, length = 20) private String status;
    @Column(length = 20) private String attendance;
    @Column(name = "confirmed_at") private Instant confirmedAt;

    protected EventParticipation() {
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UserProfile getProfile() { return profile; }
    public String getQuotaGroup() { return quotaGroup; }
    public String getNationalitySnapshot() { return nationalitySnapshot; }
    public String getStatus() { return status; }
    public String getAttendance() { return attendance; }
    public Instant getConfirmedAt() { return confirmedAt; }
}
