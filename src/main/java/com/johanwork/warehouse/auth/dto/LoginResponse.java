package com.johanwork.warehouse.auth.dto;

import com.johanwork.warehouse.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private UserResponse user;
    private String token;
}
