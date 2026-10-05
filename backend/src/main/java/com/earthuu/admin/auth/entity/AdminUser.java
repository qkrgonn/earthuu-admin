package com.earthuu.admin.auth.entity;

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
import jakarta.persistence.Version;

import com.earthuu.admin.global.exception.BusinessException;
import com.earthuu.admin.global.exception.ErrorCode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private int revision;

    protected AdminUser() {
    }

    private AdminUser(UserStatus status, UserRole role) {
        this.status = status;
        this.role = role;
    }

    public static AdminUser createAdmin() {
        return new AdminUser(UserStatus.ACTIVE, UserRole.ADMIN);
    }

    public static AdminUser createUser() {
        return new AdminUser(UserStatus.ACTIVE, UserRole.USER);
    }

    public void suspend() {
        if (role == UserRole.ADMIN) throw new BusinessException(ErrorCode.CANNOT_SANCTION_ADMIN);
        if (status != UserStatus.ACTIVE) throw new BusinessException(ErrorCode.INVALID_USER_STATUS);
        status = UserStatus.SUSPENDED;
    }

    public void restore() {
        if (role == UserRole.ADMIN) throw new BusinessException(ErrorCode.CANNOT_SANCTION_ADMIN);
        if (status != UserStatus.SUSPENDED) throw new BusinessException(ErrorCode.INVALID_USER_STATUS);
        status = UserStatus.ACTIVE;
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UserStatus getStatus() {
        return status;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public int getRevision() { return revision; }
}
