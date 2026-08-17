package com.johanwork.warehouse.notification.service.impl;

import com.johanwork.warehouse.notification.entity.NotificationOutbox;
import com.johanwork.warehouse.notification.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationOutboxClaimService {

    private final NotificationOutboxRepository repository;

    @Transactional
    public List<NotificationOutbox> claimBatch(int batchSize) {
        List<NotificationOutbox> pending = repository.findPendingForUpdateSkipLocked(PageRequest.of(0, batchSize));
        pending.forEach(NotificationOutbox::markProcessing);
        return pending;
    }
}
