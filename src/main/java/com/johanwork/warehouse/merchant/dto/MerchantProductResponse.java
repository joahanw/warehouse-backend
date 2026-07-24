package com.johanwork.warehouse.merchant.dto;

import com.johanwork.warehouse.product.dto.ProductResponse;
import com.johanwork.warehouse.warehouse.dto.WarehouseResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class MerchantProductResponse {
    private Long id;
    private MerchantResponse merchant;
    private ProductResponse product;
    private WarehouseResponse warehouse;
    private Integer stock;
}
