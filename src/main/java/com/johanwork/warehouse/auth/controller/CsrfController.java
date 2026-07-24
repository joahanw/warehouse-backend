package com.johanwork.warehouse.auth.controller;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.constant.AppConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/csrf-token")
public class CsrfController {

    @GetMapping
    public ResponseEntity<GenericResponse<CsrfToken>> getCsrfToken(HttpServletRequest request) {
        var token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericResponse<>(token, AppConstant.Success.CSRF_TOKEN));
    }

}
