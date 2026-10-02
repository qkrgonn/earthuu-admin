package com.earthuu.admin.event.repository;

import com.earthuu.admin.event.entity.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {
    @Override
    @EntityGraph(attributePaths = {"currentVersion", "hostProfile", "hostProfile.university"})
    Page<Event> findAll(Specification<Event> specification, Pageable pageable);
}
