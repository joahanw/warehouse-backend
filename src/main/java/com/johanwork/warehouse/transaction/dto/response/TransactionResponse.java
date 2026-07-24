package com.johanwork.warehouse.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private BigDecimal subTotal;
    private BigDecimal taxTotal;
    private BigDecimal grandTotal;
    private Long merchantId;
    private String merchantName;
    private String paymentStatus;
    private String paymentMethod;
    private String transactionCode;
    private String oderId;
    private String notes;

    private List<TransactionProductResponse> transactionProducts;

}
