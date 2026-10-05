package com.earthuu.admin.notification.repository;

import com.earthuu.admin.notification.entity.OutboxMessage;
import com.earthuu.admin.notification.entity.OutboxStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {
    boolean existsByDedupeKey(String dedupeKey);
    Page<OutboxMessage> findByStatusOrderByCreatedAtDesc(OutboxStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from OutboxMessage item where item.status = :status " +
            "and item.nextAttemptAt <= :now order by item.createdAt")
    List<OutboxMessage> lockDue(@Param("status") OutboxStatus status, @Param("now") Instant now, Pageable pageable);
}
