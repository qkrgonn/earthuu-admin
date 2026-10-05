package com.earthuu.admin.statistics.dto;

import java.util.List;

public record ReportStatisticsResponse(List<TimePointResponse> received,
                                       List<TimePointResponse> resolved) {}
