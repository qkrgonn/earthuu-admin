package com.earthuu.admin.report;

import com.earthuu.admin.report.entity.Report;
import com.earthuu.admin.report.entity.ReportTargetType;
import com.earthuu.admin.report.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ReportOptimisticLockingIntegrationTests {
    @Autowired ReportRepository reportRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    void staleReportReviewUpdateIsRejected() {
        var reporterId = createUser();
        var firstAdminId = createUser();
        var secondAdminId = createUser();
        var report = reportRepository.saveAndFlush(Report.create(reporterId, ReportTargetType.EVENT,
                UUID.randomUUID(), "TEST", "동시 처리 테스트", null));

        var transaction = new TransactionTemplate(transactionManager);
        var firstCopy = transaction.execute(status -> reportRepository.findById(report.getId()).orElseThrow());
        var secondCopy = transaction.execute(status -> reportRepository.findById(report.getId()).orElseThrow());

        transaction.executeWithoutResult(status -> {
            firstCopy.startReview(firstAdminId);
            reportRepository.saveAndFlush(firstCopy);
        });

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            secondCopy.startReview(secondAdminId);
            reportRepository.saveAndFlush(secondCopy);
        })).isInstanceOf(OptimisticLockingFailureException.class);
    }

    private UUID createUser() {
        var id = UUID.randomUUID();
        var now = Instant.now();
        jdbcTemplate.update("insert into users(id, status, role, created_at, updated_at) values (?, 'ACTIVE', 'USER', ?, ?)",
                id, now, now);
        return id;
    }
}
