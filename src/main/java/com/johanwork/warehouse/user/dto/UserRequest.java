package com.johanwork.warehouse.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 5, max = 30, message = "The length of the name should be between 5 and 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 20, message = "Password length must be between 8 and 20 Character")
        String password,

        String photo,

        @NotBlank(message = "Phone is required")
        @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 numbers")
        String phone
) {
}
