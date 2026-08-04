package com.johanwork.warehouse.transaction.dto.request;

import com.johanwork.warehouse.transaction.dto.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record TransactionRequest(
        @NotNull(message = "Merchant ID is required")
        Long merchantId,

        // Optional: "qris" (Midtrans, default) or "bca_qris_static"
        String paymentMethod,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Phone is required")
        String phone,

        @NotBlank(message = "Email is required")
        @Email(message = "Email is not valid")
        String email,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "Notes is required")
        String notes,

        @NotNull(message = "Shipping cost is required")
        BigDecimal shippingCost,

        @NotNull(message = "Products is required")
        List<ProductItems> products
) {
}

