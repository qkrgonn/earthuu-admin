package com.earthuu.admin.masterdata.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "cities")
public class City {
    @Id @Column(length = 30) private String code;
    @Column(name = "official_name", nullable = false, length = 100) private String officialName;
    @Column(name = "display_name_ko", nullable = false, length = 50) private String displayNameKo;
    @Column(name = "display_name_en", length = 50) private String displayNameEn;
    @Column(nullable = false) private boolean active;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected City() {}
    private City(String code, String officialName, String displayNameKo, String displayNameEn) {
        this.code = code.trim().toUpperCase(); this.officialName = officialName;
        this.displayNameKo = displayNameKo; this.displayNameEn = displayNameEn; this.active = true;
    }
    public static City create(String code, String officialName, String displayNameKo, String displayNameEn) {
        return new City(code, officialName, displayNameKo, displayNameEn);
    }
    public void update(String officialName, String displayNameKo, String displayNameEn) {
        this.officialName = officialName; this.displayNameKo = displayNameKo; this.displayNameEn = displayNameEn;
    }
    public void activate() { active = true; }
    public void deactivate() { active = false; }
    @PrePersist void prePersist() { var now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
    public String getCode() { return code; }
    public String getOfficialName() { return officialName; }
    public String getDisplayNameKo() { return displayNameKo; }
    public String getDisplayNameEn() { return displayNameEn; }
    public boolean isActive() { return active; }
}
