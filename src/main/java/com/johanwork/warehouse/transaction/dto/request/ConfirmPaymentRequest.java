package com.johanwork.warehouse.transaction.dto.request;

import com.johanwork.warehouse.transaction.dto.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record ConfirmPaymentRequest(
        @NotNull(message = "Status is required")
        PaymentStatus status
) {
}
