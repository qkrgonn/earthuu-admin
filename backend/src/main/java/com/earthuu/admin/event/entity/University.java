package com.earthuu.admin.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    protected University() {
    }

    public String getNameKo() {
        return nameKo;
    }
}
