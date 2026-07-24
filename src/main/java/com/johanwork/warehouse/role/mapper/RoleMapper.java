package com.johanwork.warehouse.role.mapper;

import com.johanwork.warehouse.common.response.GenericResponseMapper;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.role.dto.RoleRequest;
import com.johanwork.warehouse.role.dto.RoleResponse;
import com.johanwork.warehouse.role.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleMapper implements GenericResponseMapper<Role, RoleRequest, RoleResponse> {

    @Override
    public RoleResponse mapEntityToResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getUsers().size()
        );
    }

    @Override
    public Role mapRequestToEntity(RoleRequest roleRequest) {
        return new Role(
                null,
                roleRequest.name().toUpperCase(),
                null
        );
    }

    @Override
    public List<RoleResponse> mapListEntityToListResponse(List<Role> m) {
        if (null != m) {
            return m.stream()
                    .map(this::mapEntityToResponse)
                    .toList();
        }
        return List.of();
    }

    @Override
    public PageResponse<RoleResponse> mapPageEntityToPageResponse(Page<Role> m) {
        return null;
    }

}
