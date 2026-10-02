package com.earthuu.admin.event.repository;

import com.earthuu.admin.event.entity.EventVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventVersionRepository extends JpaRepository<EventVersion, UUID> {
}
