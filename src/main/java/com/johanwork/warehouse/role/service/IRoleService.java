package com.johanwork.warehouse.role.service;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.role.dto.RoleRequest;
import com.johanwork.warehouse.role.dto.RoleResponse;

import java.util.List;

public interface IRoleService {
    GenericResponse<List<RoleResponse>> getAllRoles();
    GenericResponse<RoleResponse> getRoleById(Long id);
    GenericResponse<Void> create(RoleRequest roleRequest);
    GenericResponse<Void> update(Long id, RoleRequest roleRequest);
    GenericResponse<Void> delete(Long id);
}
