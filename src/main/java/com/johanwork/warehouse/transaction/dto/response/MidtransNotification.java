package com.johanwork.warehouse.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MidtransNotification(
        @JsonProperty("order_id") String orderId,
        @JsonProperty("transaction_id") String transactionId,
        @JsonProperty("transaction_status") String transactionStatus,
        @JsonProperty("fraud_status") String fraudStatus,
        @JsonProperty("status_code") String statusCode,
        @JsonProperty("gross_amount") String grossAmount,
        @JsonProperty("signature_key") String signatureKey,
        @JsonProperty("payment_type") String paymentType,
        @JsonProperty("expiry_time") String expiryTime
) {
}
