package com.johanwork.warehouse.warehouse.dto;

import com.johanwork.warehouse.category.dto.CategoryResponse;
import com.johanwork.warehouse.product.dto.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseProductResponse {

    private Long id;
    private WarehouseResponse warehouse;
    private ProductResponse product;
    private Integer stock;

}
