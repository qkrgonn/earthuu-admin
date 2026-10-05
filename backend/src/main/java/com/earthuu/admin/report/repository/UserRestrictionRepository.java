package com.earthuu.admin.report.repository;

import com.earthuu.admin.report.entity.UserRestriction;
import com.earthuu.admin.report.entity.UserRestrictionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserRestrictionRepository extends JpaRepository<UserRestriction, UUID> {
    List<UserRestriction> findByUserIdAndStatusOrderByStartsAtDesc(UUID userId, UserRestrictionStatus status);

    List<UserRestriction> findByUserIdOrderByStartsAtDesc(UUID userId);

    java.util.Optional<UserRestriction> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndStatusAndEndsAtIsNull(UUID userId, UserRestrictionStatus status);

    boolean existsByUserIdAndStatusAndEndsAtAfter(UUID userId, UserRestrictionStatus status, Instant now);
}
