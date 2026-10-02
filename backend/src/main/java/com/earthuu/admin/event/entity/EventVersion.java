package com.earthuu.admin.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_versions")
public class EventVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_state", nullable = false, length = 20)
    private SubmissionState submissionState;

    @Column(nullable = false, length = 160)
    private String title;
    @Column(name = "thumbnail_asset_id") private UUID thumbnailAssetId;
    @Column(name = "category_id") private UUID categoryId;
    @Column(name = "city_code", length = 30) private String cityCode;
    @Column(name = "venue_name", length = 200) private String venueName;
    @Column(length = 300) private String address;
    @Column(precision = 10, scale = 7) private BigDecimal latitude;
    @Column(precision = 10, scale = 7) private BigDecimal longitude;
    @Column(name = "place_provider", length = 30) private String placeProvider;
    @Column(name = "provider_place_id", length = 255) private String providerPlaceId;
    @Column(name = "starts_at", nullable = false) private Instant startsAt;
    @Column(name = "ends_at", nullable = false) private Instant endsAt;
    @Column(nullable = false, length = 64) private String timezone;
    @Column(name = "recruitment_start") private Instant recruitmentStart;
    @Column(name = "recruitment_end") private Instant recruitmentEnd;
    @Column(name = "description_ko", columnDefinition = "text") private String descriptionKo;
    @Column(name = "description_en", columnDefinition = "text") private String descriptionEn;
    @Column(name = "submitted_at") private Instant submittedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected EventVersion() {
    }

    private EventVersion(UUID eventId, String title, Instant startsAt, Instant endsAt, Instant submittedAt) {
        this.eventId = eventId;
        this.versionNumber = 1;
        this.submissionState = SubmissionState.SUBMITTED;
        this.title = title;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.timezone = "Asia/Seoul";
        this.submittedAt = submittedAt;
    }

    public static EventVersion createSubmitted(UUID eventId, String title, Instant startsAt, Instant endsAt, Instant submittedAt) {
        return new EventVersion(eventId, title, startsAt, endsAt, submittedAt);
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public int getVersionNumber() { return versionNumber; }
    public SubmissionState getSubmissionState() { return submissionState; }
    public String getTitle() { return title; }
    public UUID getCategoryId() { return categoryId; }
    public String getCityCode() { return cityCode; }
    public String getVenueName() { return venueName; }
    public String getAddress() { return address; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public String getTimezone() { return timezone; }
    public Instant getRecruitmentStart() { return recruitmentStart; }
    public Instant getRecruitmentEnd() { return recruitmentEnd; }
    public String getDescriptionKo() { return descriptionKo; }
    public String getDescriptionEn() { return descriptionEn; }
    public Instant getSubmittedAt() { return submittedAt; }
}
