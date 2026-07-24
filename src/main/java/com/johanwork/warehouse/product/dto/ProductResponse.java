package com.johanwork.warehouse.product.dto;

import com.johanwork.warehouse.category.dto.CategoryResponse;
import com.johanwork.warehouse.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String barcode;
    private BigDecimal price;
    private String about;
    private String thumbnail;
    private Boolean isPopular;
    private CategoryResponse category;
}
