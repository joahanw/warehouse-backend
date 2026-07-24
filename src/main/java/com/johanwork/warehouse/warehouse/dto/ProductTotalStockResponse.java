package com.johanwork.warehouse.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductTotalStockResponse {
    private Long productId;
    private Long totalStock;
}
