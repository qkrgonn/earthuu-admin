package com.earthuu.admin.masterdata.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "university_domains")
public class UniversityDomain {
    @Id @Column(length = 255) private String domain;
    @Column(name = "university_id", nullable = false) private UUID universityId;
    @Column(name = "verified_by") private UUID verifiedBy;
    @Column(name = "verified_at") private Instant verifiedAt;
    @Column(nullable = false) private boolean active;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected UniversityDomain() {}
    private UniversityDomain(UUID universityId, String domain, UUID verifiedBy) {
        this.universityId = universityId;
        this.domain = normalize(domain);
        this.verifiedBy = verifiedBy;
        this.verifiedAt = Instant.now();
        this.active = true;
    }
    public static UniversityDomain create(UUID universityId, String domain, UUID verifiedBy) {
        return new UniversityDomain(universityId, domain, verifiedBy);
    }
    public void activate() { active = true; }
    public void deactivate() { active = false; }
    @PrePersist void prePersist() { var now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
    private static String normalize(String domain) { return domain.trim().toLowerCase(); }
    public String getDomain() { return domain; }
    public UUID getUniversityId() { return universityId; }
    public UUID getVerifiedBy() { return verifiedBy; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
