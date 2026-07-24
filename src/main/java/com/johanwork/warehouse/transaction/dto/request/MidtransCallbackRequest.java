package com.johanwork.warehouse.transaction.dto.request;

public record MidtransCallbackRequest(
        String orderId,
        String transactionStatus,
        String paymentType,
        String fraudStatus,
        String transactionId,
        String statusCode,
        String signatureKey
) {
}
