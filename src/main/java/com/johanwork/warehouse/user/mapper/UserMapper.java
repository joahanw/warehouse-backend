package com.johanwork.warehouse.user.mapper;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.GenericResponseMapper;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.role.entity.Role;
import com.johanwork.warehouse.role.mapper.RoleMapper;
import com.johanwork.warehouse.user.dto.UserRequest;
import com.johanwork.warehouse.user.dto.UserResponse;
import com.johanwork.warehouse.user.dto.UserRoleResponse;
import com.johanwork.warehouse.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper implements GenericResponseMapper<User, UserRequest, UserResponse> {

    private final RoleMapper roleMapper;

    @Override
    public UserResponse mapEntityToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoto(),
                user.getPhone(),
                user.getRoles().stream()
                        .map(Role::getName).collect(Collectors.joining(","))
        );
    }

    @Override
    public User mapRequestToEntity(UserRequest userRequest) {
        return new User(
                null,
                userRequest.name(),
                userRequest.email(),
                userRequest.password(),
                userRequest.photo(),
                userRequest.phone(),
                new HashSet<>(),
                null
        );
    }

    @Override
    public List<UserResponse> mapListEntityToListResponse(List<User> m) {
        if (null != m){
            return m.stream()
                    .map(this::mapEntityToResponse)
                    .toList();
        }
        return List.of();
    }

    @Override
    public PageResponse<UserResponse> mapPageEntityToPageResponse(Page<User> m) {
        if (null != m){
            return new PageResponse<>(
                    m.map(this::mapEntityToResponse).toList(),
                    m.getNumber(),
                    m.getSize(),
                    m.getTotalElements(),
                    m.getTotalPages(),
                    m.hasNext(),
                    m.hasPrevious()
            );
        }
        return new PageResponse<>();
    }

    public UserRoleResponse mapEntityToUserRoleResponse(User user){
        return new UserRoleResponse(
              mapEntityToResponse(user),
                roleMapper.mapListEntityToListResponse(user.getRoles().stream().toList())
        );
    }

    public GenericResponse<List<UserRoleResponse>> mapToListUserRoleResponse(List<User> user, String message){
        if (null != user){
            List<UserRoleResponse> userRole = user.stream()
                    .map(this::mapEntityToUserRoleResponse)
                    .toList();
            return new GenericResponse<>(userRole, message);
        }
        return new GenericResponse<>(List.of(), message);
    }

    public GenericResponse<PageResponse<UserRoleResponse>> mapToPageUserRoleResponse(Page<User> user, String message){
        if (null != user){
            var response = new PageResponse<>(
                    user.map(this::mapEntityToUserRoleResponse).toList(),
                    user.getNumber(),
                    user.getSize(),
                    user.getTotalElements(),
                    user.getTotalPages(),
                    user.hasNext(),
                    user.hasPrevious()
            );
            return new GenericResponse<>(response, message);
        }
        return new GenericResponse<>(new PageResponse<>(), message);
    }

}
