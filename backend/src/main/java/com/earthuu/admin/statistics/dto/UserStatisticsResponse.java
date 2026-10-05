package com.earthuu.admin.statistics.dto;

import java.util.List;
import java.util.Map;

public record UserStatisticsResponse(List<TimePointResponse> registrations,
                                     Map<String, Long> currentStatusCounts) {}
