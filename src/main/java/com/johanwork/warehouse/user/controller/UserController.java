package com.johanwork.warehouse.user.controller;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.user.dto.UserRequest;
import com.johanwork.warehouse.user.dto.UserResponse;
import com.johanwork.warehouse.user.dto.UserRoleResponse;
import com.johanwork.warehouse.user.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final IUserService userService;

    @GetMapping
    public ResponseEntity<GenericResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getAllUsers(pageNumber, pageSize, sortBy, sortDirection));
    }


    @GetMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<UserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUserById(id));
    }

    @GetMapping(path = "/role", version = "1.0")
    public ResponseEntity<GenericResponse<List<UserRoleResponse>>> getUserByRoleName(@RequestParam String roleName) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUserByRoleName(roleName));
    }

    @PostMapping(version = "1.0")
    public ResponseEntity<GenericResponse<Void>> createUser(@RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.create(userRequest));
    }

    @PutMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> updateUser(@PathVariable Long id,
                                                            @RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.update(id, userRequest));
    }

    @DeleteMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> deleteUser(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.delete(id));
    }

}
