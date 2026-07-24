package com.johanwork.warehouse.transaction.dto.response;

import com.johanwork.warehouse.product.dto.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionProductResponse {

    private Long id;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subTotal;
    private ProductResponse product;

}
