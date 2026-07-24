package com.johanwork.warehouse.auth.service.impl;

import com.johanwork.warehouse.auth.dto.LoginRequest;
import com.johanwork.warehouse.auth.dto.LoginResponse;
import com.johanwork.warehouse.auth.service.IAuthService;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.user.dto.UserRequest;
import com.johanwork.warehouse.user.entity.User;
import com.johanwork.warehouse.user.mapper.UserMapper;
import com.johanwork.warehouse.user.service.IUserService;
import com.johanwork.warehouse.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final IUserService userService;

    @Override
    public GenericResponse<LoginResponse> login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );
        String token = jwtUtil.generateToken(authentication);
        User user = (User) authentication.getPrincipal();
        var response = new LoginResponse(
                userMapper.mapEntityToResponse(user),
                token);
        return new GenericResponse<>(response, AppConstant.Success.LOGIN);
    }

    @Override
    public GenericResponse<Void> register(UserRequest userRequest) {
        return userService.create(userRequest);
    }

}
