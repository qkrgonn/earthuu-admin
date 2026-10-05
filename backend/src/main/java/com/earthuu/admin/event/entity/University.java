package com.earthuu.admin.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "universities")
public class University {
    @Id
    private UUID id;

    @Column(name = "name_ko", nullable = false, length = 150)
    private String nameKo;

    @Column(name = "name_en", length = 150)
    private String nameEn;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected University() {
    }

    private University(String nameKo, String nameEn) {
        this.id = UUID.randomUUID();
        this.nameKo = nameKo;
        this.nameEn = nameEn;
        this.active = true;
    }

    public static University create(String nameKo, String nameEn) { return new University(nameKo, nameEn); }

    public void update(String nameKo, String nameEn) { this.nameKo = nameKo; this.nameEn = nameEn; }
    public void activate() { active = true; }
    public void deactivate() { active = false; }

    @PrePersist void prePersist() { var now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }

    public String getNameKo() {
        return nameKo;
    }

    public String getNameEn() { return nameEn; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
