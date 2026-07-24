package com.johanwork.warehouse.notification.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceEmailDto(
        String invoiceId,
        String customerName,
        String email,
        String orderId,
        LocalDateTime paidAt,
        BigDecimal subTotal,
        BigDecimal taxTotal,
        BigDecimal grandTotal,
        BigDecimal shippingCost,
        String paymentMethod,
        String address,
        List<InvoiceItem> items
) {
    public record InvoiceItem(
            String productName,
            Integer quantity,
            String subTotal
    ){}
}
