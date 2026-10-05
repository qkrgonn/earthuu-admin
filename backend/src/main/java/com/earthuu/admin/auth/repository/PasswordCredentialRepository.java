package com.earthuu.admin.auth.repository;

import com.earthuu.admin.auth.entity.PasswordCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordCredentialRepository extends JpaRepository<PasswordCredential, UUID> {
    Optional<PasswordCredential> findByLoginEmailIgnoreCase(String loginEmail);

    Optional<PasswordCredential> findByUserId(UUID userId);

    boolean existsByLoginEmailIgnoreCase(String loginEmail);
}
