package com.johanwork.warehouse.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetailWarehouseProductResponse {

    private Long id;
    private String name;
    private String address;
    private String photo;
    private String phone;
    private List<WarehouseProductResponse> warehouseProduct;

}
