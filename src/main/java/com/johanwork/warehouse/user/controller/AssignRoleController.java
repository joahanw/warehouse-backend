package com.johanwork.warehouse.user.controller;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.user.dto.UserRoleResponse;
import com.johanwork.warehouse.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assign-role")
@RequiredArgsConstructor
public class AssignRoleController {

    private final IUserService userService;

    @GetMapping(version = "1.0")
    public ResponseEntity<GenericResponse<PageResponse<UserRoleResponse>>> getAllUserRoles(
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getAllUserRole(pageNumber, pageSize, sortBy, sortDirection));
    }

    @PostMapping(version = "1.0")
    public ResponseEntity<GenericResponse<Void>> assignRole(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "roleId") Long roleId
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.assignUserToRole(userId, roleId));
    }

    @PutMapping(version = "1.0")
    public ResponseEntity<GenericResponse<Void>> editAssignRole(
            @RequestParam Long userId,
            @RequestParam Long roleId,
            @RequestParam Long assignedRoleId
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.updateUserRole(userId, roleId, assignedRoleId));
    }

}
