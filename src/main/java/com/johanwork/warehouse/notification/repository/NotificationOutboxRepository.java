package com.johanwork.warehouse.notification.repository;

import com.johanwork.warehouse.notification.entity.NotificationOutbox;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
            select n from NotificationOutbox n
            where n.status = com.johanwork.warehouse.notification.dto.NotificationStatus.PENDING
            order by n.createdAt asc
            """)
    List<NotificationOutbox> findPendingForUpdateSkipLocked(Pageable pageable);
}
