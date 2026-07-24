package com.johanwork.warehouse.user.dto;

import com.johanwork.warehouse.role.dto.RoleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleResponse{
    private UserResponse user;
    private List<RoleResponse> role;
}
