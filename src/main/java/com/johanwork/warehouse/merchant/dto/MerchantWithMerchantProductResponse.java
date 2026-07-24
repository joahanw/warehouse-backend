package com.johanwork.warehouse.merchant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class MerchantWithMerchantProductResponse {
    private Long id;
    private String name;
    private String address;
    private String photo;
    private String phone;
    private Long keeperId;
    private String keeperName;
    private List<MerchantProductResponse> merchantProducts;
}
