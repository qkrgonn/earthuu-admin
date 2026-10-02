package com.earthuu.admin.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "profiles")
public class UserProfile {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, length = 80)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    private University university;

    protected UserProfile() {
    }

    public String getName() {
        return name;
    }

    public String getUniversityName() {
        return university == null ? null : university.getNameKo();
    }
}
