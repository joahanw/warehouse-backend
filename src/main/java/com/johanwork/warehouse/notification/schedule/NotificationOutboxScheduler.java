package com.johanwork.warehouse.notification.schedule;


import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.johanwork.warehouse.common.config.configProps.WahaAppProperties;
import com.johanwork.warehouse.notification.entity.NotificationOutbox;
import com.johanwork.warehouse.notification.repository.NotificationOutboxRepository;
import com.johanwork.warehouse.notification.service.impl.NotificationOutboxClaimService;
import com.johanwork.warehouse.notification.service.impl.WahaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationOutboxScheduler {

    private static final int BATCH_SIZE = 50;

    private final NotificationOutboxRepository repository;
    private final NotificationOutboxClaimService claimService;
    private final WahaService service;
    private final WahaAppProperties props;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 10_000)
    public void processQueue(){
        List<NotificationOutbox> claimed = claimService.claimBatch(BATCH_SIZE);
        if (claimed.isEmpty()){
            return;
        }
        log.info("Processing {} pending notifications", claimed.size());
        for (NotificationOutbox item : claimed){
            sendOne(item);
            sleepRandom();
        }
    }

    void sendOne(NotificationOutbox item) {
        try {
            List<String> params = objectMapper.readValue(
                    item.getParamJson(), new TypeReference<List<String>>() {});
            String caption = item.getTemplate().render(params);

            if (item.getImageUrl() != null) {
                service.sendImageUrl(item.getPhone(), item.getImageUrl(), caption);
            } else {
                service.sendTemplate(item.getPhone(), item.getTemplate(), params);
            }

            item.markSent();
            log.info("Notification {} send to {}", item.getId(), item.getPhone());

        } catch (Exception ex) {
            item.markFailed(ex.getMessage());
            log.error("Notification {} failed (attempt-{}): {}",
                    item.getId(), item.getAttempt(), ex.getMessage());
            // TODO: kalau item.getStatus() == FAILED (sudah 5x), kirim alert
        } finally {
            repository.save(item);
        }
    }

    private void sleepRandom() {
        try {
            long delay = ThreadLocalRandom.current().nextLong(props.minDelayMs(), props.maxDelayMs());
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
