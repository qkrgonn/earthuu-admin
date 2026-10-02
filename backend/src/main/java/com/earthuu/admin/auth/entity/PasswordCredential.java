package com.earthuu.admin.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_credentials")
public class PasswordCredential {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "login_email", nullable = false, unique = true, length = 320)
    private String loginEmail;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PasswordCredential() {
    }

    private PasswordCredential(UUID userId, String loginEmail, String passwordHash) {
        this.userId = userId;
        this.loginEmail = loginEmail.toLowerCase();
        this.passwordHash = passwordHash;
    }

    public static PasswordCredential create(UUID userId, String loginEmail, String passwordHash) {
        return new PasswordCredential(userId, loginEmail, passwordHash);
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

    public UUID getUserId() {
        return userId;
    }

    public String getLoginEmail() {
        return loginEmail;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
