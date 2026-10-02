package com.earthuu.admin.event.repository;

import com.earthuu.admin.event.entity.ModerationAction;
import com.earthuu.admin.event.entity.ModerationActionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {
    List<ModerationAction> findByEventIdOrderByCreatedAtDesc(UUID eventId);

    List<ModerationAction> findByActorIdAndActionInOrderByCreatedAtDesc(
            UUID actorId,
            Collection<ModerationActionType> actions
    );
}
