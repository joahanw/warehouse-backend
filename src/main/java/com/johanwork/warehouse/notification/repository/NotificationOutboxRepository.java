package com.johanwork.warehouse.notification.repository;

import com.johanwork.warehouse.notification.entity.NotificationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {
    @Query("""
            select n from NotificationOutbox n
            where n.status = com.johanwork.warehouse.notification.dto.NotificationStatus.PENDING
            order by n.createdAt asc
            """)
    List<NotificationOutbox> findPendingOrderByOldest();
}
