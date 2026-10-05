package com.earthuu.admin.report.dto;

import com.earthuu.admin.report.entity.ReportStatus;
import com.earthuu.admin.report.entity.ReportTargetType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public class ReportSearchRequest {
    private String query;
    private ReportTargetType targetType;
    private ReportStatus status;
    private LocalDate reportedFrom;
    private LocalDate reportedTo;
    private ReportSort sort = ReportSort.NEWEST;
    @Min(0) private int page = 0;
    @Min(1) @Max(100) private int size = 20;

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public ReportTargetType getTargetType() { return targetType; }
    public void setTargetType(ReportTargetType targetType) { this.targetType = targetType; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public LocalDate getReportedFrom() { return reportedFrom; }
    public void setReportedFrom(LocalDate reportedFrom) { this.reportedFrom = reportedFrom; }
    public LocalDate getReportedTo() { return reportedTo; }
    public void setReportedTo(LocalDate reportedTo) { this.reportedTo = reportedTo; }
    public ReportSort getSort() { return sort; }
    public void setSort(ReportSort sort) { this.sort = sort; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
