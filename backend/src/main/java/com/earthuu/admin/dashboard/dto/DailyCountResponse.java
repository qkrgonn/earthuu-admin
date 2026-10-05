package com.earthuu.admin.dashboard.dto;

import java.time.LocalDate;

public record DailyCountResponse(LocalDate date, long count) {}
