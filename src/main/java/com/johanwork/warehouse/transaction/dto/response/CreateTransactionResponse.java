package com.johanwork.warehouse.transaction.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionResponse(
        Long transactionId,
        String orderId,
        String qrCodeUrl,
        String expiryTime,
        BigDecimal grandTotal,
        LocalDate deliveryDate
) {
}
