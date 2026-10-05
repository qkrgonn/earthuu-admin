package com.earthuu.admin.statistics.dto;

import java.time.LocalDate;

public class StatisticsQueryRequest {
    private LocalDate from;
    private LocalDate to;
    private StatisticsGranularity granularity = StatisticsGranularity.DAY;

    public LocalDate getFrom() { return from; }
    public void setFrom(LocalDate from) { this.from = from; }
    public LocalDate getTo() { return to; }
    public void setTo(LocalDate to) { this.to = to; }
    public StatisticsGranularity getGranularity() { return granularity; }
    public void setGranularity(StatisticsGranularity granularity) { this.granularity = granularity; }
}
