package com.earthuu.admin.dashboard.dto;

import java.util.List;

public record DashboardResponse(QueueSummary queues, UserSummary users,
                                List<DailyCountResponse> registrations,
                                List<AdminActivityResponse> recentActivities) {
    public record QueueSummary(long pendingEvents, long reviewingEvents, long unresolvedReports,
                               long pendingVerifications) {}
    public record UserSummary(long active, long suspended, long withdrawn) {}
}
