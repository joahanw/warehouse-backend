package com.johanwork.warehouse.notification.entity;

import com.johanwork.warehouse.notification.dto.NotificationStatus;
import com.johanwork.warehouse.notification.dto.WhatsAppTemplate;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "notification_outbox")
public class NotificationOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;

    @Enumerated(EnumType.STRING)
    private WhatsAppTemplate template;

    @Column(columnDefinition = "text")
    private String paramJson;

    @Column(length = 100)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.PENDING;

    private int attempt = 0;

    @Column(columnDefinition = "text")
    private String lastError;

    private Instant createdAt = Instant.now();

    private Instant sentAt;

    public NotificationOutbox(String phone, WhatsAppTemplate template, String paramJson) {
       this(phone, template, paramJson, null);
    }

    public NotificationOutbox(String phone, WhatsAppTemplate template, String paramJson, String imageUrl) {
        this.phone = phone;
        this.template = template;
        this.paramJson = paramJson;
        this.imageUrl = imageUrl;
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
    }

    public void markFailed(String error) {
        this.attempt++;
        this.lastError = error == null ? null : error.substring(0, Math.min(error.length(), 2000));
        if (this.attempt >=5) {
            this.status = NotificationStatus.FAILED;
        }
    }

    public void resetForRetry() {
        this.status = NotificationStatus.PENDING;
    }

}
