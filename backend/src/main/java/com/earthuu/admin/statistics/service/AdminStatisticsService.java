package com.earthuu.admin.statistics.service;

import com.earthuu.admin.global.exception.BusinessException;
import com.earthuu.admin.global.exception.ErrorCode;
import com.earthuu.admin.statistics.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminStatisticsService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final JdbcTemplate jdbcTemplate;

    public AdminStatisticsService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @Transactional(readOnly = true)
    public StatisticsOverviewResponse overview(StatisticsQueryRequest request) {
        var range = range(request);
        return new StatisticsOverviewResponse(range.from, range.to,
                count("select count(*) from users where role = 'USER' and created_at >= ? and created_at < ?", range),
                count("select count(*) from events where created_at >= ? and created_at < ?", range),
                count("select count(*) from reports where created_at >= ? and created_at < ?", range),
                count("select count(*) from student_verifications where created_at >= ? and created_at < ?", range));
    }

    @Transactional(readOnly = true)
    public UserStatisticsResponse users(StatisticsQueryRequest request) {
        var range = range(request);
        var series = series("select cast(created_at as date) bucket_date, count(*) total from users " +
                "where role = 'USER' and created_at >= ? and created_at < ? group by cast(created_at as date)", range);
        var statuses = new LinkedHashMap<String, Long>();
        jdbcTemplate.query("select status, count(*) total from users where role = 'USER' group by status",
                (RowCallbackHandler) resultSet -> statuses.put(
                        resultSet.getString("status"), resultSet.getLong("total")));
        return new UserStatisticsResponse(series, statuses);
    }

    @Transactional(readOnly = true)
    public EventStatisticsResponse events(StatisticsQueryRequest request) {
        var range = range(request);
        var created = series("select cast(created_at as date) bucket_date, count(*) total from events " +
                "where created_at >= ? and created_at < ? group by cast(created_at as date)", range);
        var approved = series("select cast(created_at as date) bucket_date, count(*) total from moderation_actions " +
                "where action = 'APPROVED' and created_at >= ? and created_at < ? group by cast(created_at as date)", range);
        var rejected = series("select cast(created_at as date) bucket_date, count(*) total from moderation_actions " +
                "where action = 'REJECTED' and created_at >= ? and created_at < ? group by cast(created_at as date)", range);
        return new EventStatisticsResponse(created, approved, rejected);
    }

    @Transactional(readOnly = true)
    public ReportStatisticsResponse reports(StatisticsQueryRequest request) {
        var range = range(request);
        var received = series("select cast(created_at as date) bucket_date, count(*) total from reports " +
                "where created_at >= ? and created_at < ? group by cast(created_at as date)", range);
        var resolved = series("select cast(resolved_at as date) bucket_date, count(*) total from reports " +
                "where resolved_at is not null and resolved_at >= ? and resolved_at < ? group by cast(resolved_at as date)", range);
        return new ReportStatisticsResponse(received, resolved);
    }

    private List<TimePointResponse> series(String sql, Range range) {
        var values = new LinkedHashMap<String, Long>();
        for (var day = range.from; !day.isAfter(range.to); day = day.plusDays(1)) {
            values.putIfAbsent(bucket(day, range.granularity), 0L);
        }
        jdbcTemplate.query(sql, (RowCallbackHandler) resultSet -> {
            var key = bucket(resultSet.getDate("bucket_date").toLocalDate(), range.granularity);
            values.merge(key, resultSet.getLong("total"), Long::sum);
        }, range.start(), range.endExclusive());
        return values.entrySet().stream().map(entry -> new TimePointResponse(entry.getKey(), entry.getValue())).toList();
    }

    private String bucket(LocalDate date, StatisticsGranularity granularity) {
        return switch (granularity) {
            case DAY -> date.toString();
            case WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString();
            case MONTH -> YearMonth.from(date).toString();
        };
    }

    private long count(String sql, Range range) {
        var value = jdbcTemplate.queryForObject(sql, Long.class, range.start(), range.endExclusive());
        return value == null ? 0 : value;
    }

    private Range range(StatisticsQueryRequest request) {
        var today = LocalDate.now(KST);
        var from = request.getFrom() == null ? today.minusDays(29) : request.getFrom();
        var to = request.getTo() == null ? today : request.getTo();
        var granularity = request.getGranularity() == null ? StatisticsGranularity.DAY : request.getGranularity();
        if (from.isAfter(to) || from.plusYears(1).isBefore(to)) {
            throw new BusinessException(ErrorCode.INVALID_STATISTICS_DATE_RANGE);
        }
        return new Range(from, to, granularity);
    }

    private record Range(LocalDate from, LocalDate to, StatisticsGranularity granularity) {
        Timestamp start() { return Timestamp.from(from.atStartOfDay(KST).toInstant()); }
        Timestamp endExclusive() { return Timestamp.from(to.plusDays(1).atStartOfDay(KST).toInstant()); }
    }
}
