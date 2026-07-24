package com.johanwork.warehouse.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Tagline is required")
        String tagline,

        @NotBlank(message = "Photo is required")
        String photo) {
}
