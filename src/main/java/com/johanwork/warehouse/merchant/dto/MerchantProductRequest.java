package com.johanwork.warehouse.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MerchantProductRequest(

        @NotNull(message = "MerchantId is required")
        Long merchantId,

        @NotNull(message = "ProductId is required")
        Long productId,

        @NotNull(message = "WarehouseId is required")
        Long warehouseId,

        @NotNull(message = "Stock is required")
        Integer stock
) {
}
