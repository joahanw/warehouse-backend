package com.johanwork.warehouse.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MerchantRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "Photo is required")
        String photo,

        @NotBlank(message = "Phone is required")
        String phone,

        @NotNull(message = "KeeperId is required")
        Long keeperId
) {
}
