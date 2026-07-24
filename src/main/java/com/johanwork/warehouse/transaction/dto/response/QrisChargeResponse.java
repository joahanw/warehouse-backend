package com.johanwork.warehouse.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record QrisChargeResponse(
        @JsonProperty("transaction_id") String transactionId,
        @JsonProperty("order_id") String orderId,
        @JsonProperty("transaction_status") String transactionStatus,
        @JsonProperty("expiry_time") String expiryTime,
        @JsonProperty("actions") List<QrisAction> actions
) {
    public record QrisAction(
           @JsonProperty("name") String name,
           @JsonProperty("method") String method,
           @JsonProperty("url")  String url
    ) {}

    // Ambil URL QR code dari actions
    public String qrCodeUrl() {
        if (actions == null) return null;
        return actions.stream()
                .filter(a -> "generate-qr-code".equals(a.name()))
                .map(QrisAction::url)
                .findFirst()
                .orElse(null);
    }
}