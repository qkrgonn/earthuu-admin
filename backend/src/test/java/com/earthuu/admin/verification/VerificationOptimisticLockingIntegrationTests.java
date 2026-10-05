package com.earthuu.admin.verification;

import com.earthuu.admin.verification.entity.StudentVerification;
import com.earthuu.admin.verification.repository.StudentVerificationRepository;
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
class VerificationOptimisticLockingIntegrationTests {
    @Autowired StudentVerificationRepository verificationRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    void staleVerificationReviewIsRejected() {
        var userId = UUID.randomUUID();
        var reviewerId = UUID.randomUUID();
        var universityId = UUID.randomUUID();
        var now = Instant.now();

        jdbcTemplate.update("insert into users(id, status, role, created_at, updated_at) values (?, 'ACTIVE', 'USER', ?, ?)",
                userId, now, now);
        jdbcTemplate.update("insert into users(id, status, role, created_at, updated_at) values (?, 'ACTIVE', 'ADMIN', ?, ?)",
                reviewerId, now, now);
        jdbcTemplate.update("insert into universities(id, name_ko, name_en, active, created_at, updated_at) values (?, ?, ?, true, ?, ?)",
                universityId, "동시 인증 대학교", "Concurrent Verification University", now, now);

        var verification = verificationRepository.saveAndFlush(StudentVerification.pending(
                userId, "student@university.ac.kr", universityId, now, reviewerId));

        var transaction = new TransactionTemplate(transactionManager);
        var firstReviewerCopy = transaction.execute(status ->
                verificationRepository.findById(verification.getId()).orElseThrow());
        var secondReviewerCopy = transaction.execute(status ->
                verificationRepository.findById(verification.getId()).orElseThrow());

        transaction.executeWithoutResult(status -> {
            firstReviewerCopy.approve(reviewerId, now.plusSeconds(1));
            verificationRepository.saveAndFlush(firstReviewerCopy);
        });

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            secondReviewerCopy.reject(reviewerId, "동시 반려", now.plusSeconds(2));
            verificationRepository.saveAndFlush(secondReviewerCopy);
        })).isInstanceOf(OptimisticLockingFailureException.class);
    }
}
