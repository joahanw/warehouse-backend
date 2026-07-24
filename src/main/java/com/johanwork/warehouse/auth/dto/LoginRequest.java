package com.johanwork.warehouse.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        String username,
        String password
) {
}
