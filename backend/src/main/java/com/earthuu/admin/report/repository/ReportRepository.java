package com.earthuu.admin.report.repository;

import com.earthuu.admin.report.entity.Report;
import com.earthuu.admin.report.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID>, JpaSpecificationExecutor<Report> {
    List<Report> findByResolvedByAndStatusOrderByResolvedAtDesc(UUID resolvedBy, ReportStatus status);
}
