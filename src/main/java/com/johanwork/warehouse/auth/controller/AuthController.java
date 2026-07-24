package com.johanwork.warehouse.auth.controller;

import com.johanwork.warehouse.auth.dto.LoginRequest;
import com.johanwork.warehouse.auth.dto.LoginResponse;
import com.johanwork.warehouse.auth.service.IAuthService;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.user.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping(path = "/login", version = "1.0")
    public ResponseEntity<GenericResponse<LoginResponse>>login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authService.login(loginRequest));
    }

    @PostMapping(path = "/register", version = "1.0")
    public ResponseEntity<GenericResponse<Void>>register(@RequestBody UserRequest userRequest) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authService.register(userRequest));
    }

}
