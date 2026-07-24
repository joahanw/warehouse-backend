package com.johanwork.warehouse.user.service;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.user.dto.UserRequest;
import com.johanwork.warehouse.user.dto.UserResponse;
import com.johanwork.warehouse.user.dto.UserRoleResponse;

import java.util.List;

public interface IUserService {
    GenericResponse<PageResponse<UserResponse>> getAllUsers(int pageNumber, int pageSize, String sortBy, String sortDirection);
    GenericResponse<UserResponse> getUserById(Long id);
    GenericResponse<Void> create(UserRequest userRequest);
    GenericResponse<Void> update(Long id, UserRequest userRequest);
    GenericResponse<Void> delete(Long id);
    GenericResponse<List<UserRoleResponse>> getUserByRoleName(String roleName);

    GenericResponse<PageResponse<UserRoleResponse>> getAllUserRole(int pageNumber, int pageSize, String sortBy, String sortDirection);
    GenericResponse<Void> assignUserToRole(Long userId, Long roleId);
    GenericResponse<Void> updateUserRole(Long userId, Long roleId, Long assignedRoleId);
}
