package com.johanwork.warehouse.warehouse.dto;

import jakarta.validation.constraints.NotBlank;

public record WarehouseRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "Photo is required")
        String photo,

        @NotBlank(message = "Phone is required")
        String phone
) {
}
