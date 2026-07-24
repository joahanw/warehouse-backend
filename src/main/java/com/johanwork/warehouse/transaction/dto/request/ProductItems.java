package com.johanwork.warehouse.transaction.dto.request;

import java.math.BigDecimal;

public record ProductItems(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal price
){}
