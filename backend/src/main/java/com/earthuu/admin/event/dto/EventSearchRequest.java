package com.earthuu.admin.event.dto;

import com.earthuu.admin.event.entity.LifecycleStatus;
import com.earthuu.admin.event.entity.ModerationStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class EventSearchRequest {
    private String query;
    private ModerationStatus moderationStatus;
    private LifecycleStatus lifecycleStatus;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate submittedFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate submittedTo;
    private EventSort sort = EventSort.OLDEST_SUBMITTED;
    @Min(0) private int page = 0;
    @Min(1) @Max(100) private int size = 20;

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public ModerationStatus getModerationStatus() { return moderationStatus; }
    public void setModerationStatus(ModerationStatus moderationStatus) { this.moderationStatus = moderationStatus; }
    public LifecycleStatus getLifecycleStatus() { return lifecycleStatus; }
    public void setLifecycleStatus(LifecycleStatus lifecycleStatus) { this.lifecycleStatus = lifecycleStatus; }
    public LocalDate getSubmittedFrom() { return submittedFrom; }
    public void setSubmittedFrom(LocalDate submittedFrom) { this.submittedFrom = submittedFrom; }
    public LocalDate getSubmittedTo() { return submittedTo; }
    public void setSubmittedTo(LocalDate submittedTo) { this.submittedTo = submittedTo; }
    public EventSort getSort() { return sort; }
    public void setSort(EventSort sort) { this.sort = sort; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
