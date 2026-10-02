package com.earthuu.admin.event;

import com.earthuu.admin.event.entity.Event;
import com.earthuu.admin.event.entity.EventVersion;
import com.earthuu.admin.event.repository.EventRepository;
import com.earthuu.admin.event.repository.EventVersionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class EventOptimisticLockingIntegrationTests {
    @Autowired EventRepository eventRepository;
    @Autowired EventVersionRepository versionRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    void staleReviewerUpdateIsRejected() {
        var hostId = UUID.randomUUID();
        var now = Instant.now();
        jdbcTemplate.update("insert into users(id, status, role, created_at, updated_at) values (?, 'ACTIVE', 'USER', ?, ?)",
                hostId, now, now);

        var event = eventRepository.saveAndFlush(Event.createPending(hostId));
        var version = versionRepository.saveAndFlush(EventVersion.createSubmitted(
                event.getId(), "동시 심사 테스트", now.plus(7, ChronoUnit.DAYS),
                now.plus(7, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS), now));
        event.attachCurrentVersion(version.getId());
        eventRepository.saveAndFlush(event);

        var transaction = new TransactionTemplate(transactionManager);
        var firstReviewerCopy = transaction.execute(status -> eventRepository.findById(event.getId()).orElseThrow());
        var secondReviewerCopy = transaction.execute(status -> eventRepository.findById(event.getId()).orElseThrow());

        transaction.executeWithoutResult(status -> {
            firstReviewerCopy.startReview();
            eventRepository.saveAndFlush(firstReviewerCopy);
        });

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            secondReviewerCopy.startReview();
            eventRepository.saveAndFlush(secondReviewerCopy);
        })).isInstanceOf(OptimisticLockingFailureException.class);
    }
}
