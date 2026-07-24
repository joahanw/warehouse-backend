package com.johanwork.warehouse.notification.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentPendingDto(
        String customerName,
        String orderId,
        String qrCodeUrl,
        LocalDateTime expiryTime,
        BigDecimal grandTotal,
        BigDecimal subTotal,
        BigDecimal shippingCost,
        String address,
        List<OrderItem> items
) {
    public record OrderItem(
            String productName,
            Integer quantity,
            String subTotal
    ){}
}
