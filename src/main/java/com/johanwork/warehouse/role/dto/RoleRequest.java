package com.johanwork.warehouse.role.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(
        @NotBlank(message = "Name is required")
        String name
) {
}
