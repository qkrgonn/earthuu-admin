package com.earthuu.admin.user.repository;

import com.earthuu.admin.user.entity.UserStatusAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserStatusActionRepository extends JpaRepository<UserStatusAction, UUID> {
    List<UserStatusAction> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
