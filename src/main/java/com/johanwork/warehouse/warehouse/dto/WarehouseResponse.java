package com.johanwork.warehouse.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseResponse {
    private Long id;
    private String name;
    private String address;
    private String photo;
    private String phone;
    private Long countProduct;
}
