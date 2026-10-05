package com.earthuu.admin.verification.dto;

import com.earthuu.admin.verification.entity.VerificationStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.util.UUID;

public class VerificationSearchRequest {
    private String query;
    private VerificationStatus status;
    private UUID universityId;
    private LocalDate submittedFrom;
    private LocalDate submittedTo;
    private VerificationSort sort = VerificationSort.NEWEST;
    @Min(0) private int page = 0;
    @Min(1) @Max(100) private int size = 20;

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public VerificationStatus getStatus() { return status; }
    public void setStatus(VerificationStatus status) { this.status = status; }
    public UUID getUniversityId() { return universityId; }
    public void setUniversityId(UUID universityId) { this.universityId = universityId; }
    public LocalDate getSubmittedFrom() { return submittedFrom; }
    public void setSubmittedFrom(LocalDate submittedFrom) { this.submittedFrom = submittedFrom; }
    public LocalDate getSubmittedTo() { return submittedTo; }
    public void setSubmittedTo(LocalDate submittedTo) { this.submittedTo = submittedTo; }
    public VerificationSort getSort() { return sort; }
    public void setSort(VerificationSort sort) { this.sort = sort; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
