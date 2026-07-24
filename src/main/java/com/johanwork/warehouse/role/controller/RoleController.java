package com.johanwork.warehouse.role.controller;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.role.dto.RoleRequest;
import com.johanwork.warehouse.role.dto.RoleResponse;
import com.johanwork.warehouse.role.service.IRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService roleService;

    @GetMapping(version = "1.0")
    public ResponseEntity<GenericResponse<List<RoleResponse>>> getAllRoles() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.getAllRoles());
    }

    @GetMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<RoleResponse>> getRoleById(@PathVariable Long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.getRoleById(id));
    }

    @PostMapping(version = "1.0")
    public ResponseEntity<GenericResponse<Void>> create(@RequestBody @Valid RoleRequest roleRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roleService.create(roleRequest));
    }

    @PutMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> update(@PathVariable Long id,
                                                        @RequestBody @Valid RoleRequest roleRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.update(id, roleRequest));
    }

    @DeleteMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.delete(id));
    }

}
