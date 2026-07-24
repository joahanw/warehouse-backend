package com.johanwork.warehouse.auth.service;

import com.johanwork.warehouse.auth.dto.LoginRequest;
import com.johanwork.warehouse.auth.dto.LoginResponse;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.user.dto.UserRequest;

public interface IAuthService {
    GenericResponse<LoginResponse> login(LoginRequest loginRequest);
    GenericResponse<Void> register(UserRequest userRequest);
}
