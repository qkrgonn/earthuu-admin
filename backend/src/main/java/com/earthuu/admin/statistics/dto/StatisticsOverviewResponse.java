package com.earthuu.admin.statistics.dto;

import java.time.LocalDate;

public record StatisticsOverviewResponse(LocalDate from, LocalDate to,
                                         long newUsers, long newEvents, long receivedReports,
                                         long verificationRequests) {}
