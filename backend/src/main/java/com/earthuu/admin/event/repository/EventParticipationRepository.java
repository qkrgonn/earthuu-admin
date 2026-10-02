package com.earthuu.admin.event.repository;

import com.earthuu.admin.event.entity.EventParticipation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventParticipationRepository extends JpaRepository<EventParticipation, UUID> {
    @EntityGraph(attributePaths = "profile")
    List<EventParticipation> findByEventIdOrderByConfirmedAtAsc(UUID eventId);
}
