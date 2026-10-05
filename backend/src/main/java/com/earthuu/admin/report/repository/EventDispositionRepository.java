package com.earthuu.admin.report.repository;

import com.earthuu.admin.report.entity.EventDisposition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventDispositionRepository extends JpaRepository<EventDisposition, UUID> {
    List<EventDisposition> findByReportIdOrderByCreatedAtDesc(UUID reportId);
}
