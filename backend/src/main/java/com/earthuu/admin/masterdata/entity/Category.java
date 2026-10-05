package com.earthuu.admin.masterdata.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 50) private String code;
    @Column(name = "name_ko", nullable = false, length = 80) private String nameKo;
    @Column(name = "name_en", length = 80) private String nameEn;
    @Column(nullable = false) private boolean active;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected Category() {}
    private Category(String code, String nameKo, String nameEn) {
        this.id = UUID.randomUUID(); this.code = code.trim().toUpperCase();
        this.nameKo = nameKo; this.nameEn = nameEn; this.active = true;
    }
    public static Category create(String code, String nameKo, String nameEn) { return new Category(code, nameKo, nameEn); }
    public void update(String nameKo, String nameEn) { this.nameKo = nameKo; this.nameEn = nameEn; }
    public void activate() { active = true; }
    public void deactivate() { active = false; }
    @PrePersist void prePersist() { var now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getNameKo() { return nameKo; }
    public String getNameEn() { return nameEn; }
    public boolean isActive() { return active; }
}
