package com.earthuu.admin.statistics.dto;

import java.util.List;

public record EventStatisticsResponse(List<TimePointResponse> created,
                                      List<TimePointResponse> approved,
                                      List<TimePointResponse> rejected) {}
