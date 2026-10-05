package com.earthuu.admin.dashboard.service;

import com.earthuu.admin.dashboard.dto.AdminActivityResponse;
import com.earthuu.admin.dashboard.dto.DailyCountResponse;
import com.earthuu.admin.dashboard.dto.DashboardResponse;
import com.earthuu.admin.global.audit.AuditLogRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.*;
import java.util.LinkedHashMap;

@Service
public class AdminDashboardService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final JdbcTemplate jdbcTemplate;
    private final AuditLogRepository auditLogRepository;

    public AdminDashboardService(JdbcTemplate jdbcTemplate, AuditLogRepository auditLogRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        var queues = new DashboardResponse.QueueSummary(
                count("select count(*) from events where moderation_status = 'PENDING'"),
                count("select count(*) from events where moderation_status = 'REVIEWING'"),
                count("select count(*) from reports where status <> 'RESOLVED'"),
                count("select count(*) from student_verifications where status = 'PENDING'"));
        var users = new DashboardResponse.UserSummary(
                count("select count(*) from users where role = 'USER' and status = 'ACTIVE'"),
                count("select count(*) from users where role = 'USER' and status = 'SUSPENDED'"),
                count("select count(*) from users where role = 'USER' and status = 'WITHDRAWN'"));
        var activities = auditLogRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(AdminActivityResponse::from).toList();
        return new DashboardResponse(queues, users, recentRegistrations(), activities);
    }

    private java.util.List<DailyCountResponse> recentRegistrations() {
        var today = LocalDate.now(KST);
        var from = today.minusDays(6);
        var values = new LinkedHashMap<LocalDate, Long>();
        for (var day = from; !day.isAfter(today); day = day.plusDays(1)) values.put(day, 0L);
        jdbcTemplate.query("select cast(created_at as date) as bucket_date, count(*) as total " +
                        "from users where role = 'USER' and created_at >= ? and created_at < ? " +
                        "group by cast(created_at as date)",
                (RowCallbackHandler) resultSet -> values.put(
                        resultSet.getDate("bucket_date").toLocalDate(), resultSet.getLong("total")),
                Timestamp.from(from.atStartOfDay(KST).toInstant()),
                Timestamp.from(today.plusDays(1).atStartOfDay(KST).toInstant()));
        return values.entrySet().stream().map(entry -> new DailyCountResponse(entry.getKey(), entry.getValue())).toList();
    }

    private long count(String sql) {
        var value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0 : value;
    }
}
