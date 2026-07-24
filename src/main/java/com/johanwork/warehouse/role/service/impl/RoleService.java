package com.johanwork.warehouse.role.service.impl;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.role.dto.RoleRequest;
import com.johanwork.warehouse.role.dto.RoleResponse;
import com.johanwork.warehouse.role.entity.Role;
import com.johanwork.warehouse.role.mapper.RoleMapper;
import com.johanwork.warehouse.role.repository.RoleRepository;
import com.johanwork.warehouse.role.service.IRoleDomainService;
import com.johanwork.warehouse.role.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService implements IRoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final IRoleDomainService domainService;

    @Cacheable("role-list")
    @Override
    public GenericResponse<List<RoleResponse>> getAllRoles() {
        return roleMapper
                .mapToListGenericResponse(roleRepository.findAll(),
                        String.format(AppConstant.Success.FETCHED,"Roles"));
    }

    @Override
    public GenericResponse<RoleResponse> getRoleById(Long id) {
        return roleMapper.mapToGenericResponse(domainService.findRoleById(id),
                String.format(AppConstant.Success.FETCHED,"Roles"));
    }

    @Caching(evict = {
            @CacheEvict(value = "role-list", allEntries = true),
            @CacheEvict(value = "roles", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> create(RoleRequest roleRequest) {
        Role role = roleMapper.mapRequestToEntity(roleRequest);
        roleRepository.save(role);
        return new GenericResponse<>(null, String.format(AppConstant.Success.CREATED,"Role"));
    }

    @Caching(evict = {
            @CacheEvict(value = "role-list", allEntries = true),
            @CacheEvict(value = "roles", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> update(Long id, RoleRequest roleRequest) {
        Role role = domainService.findRoleById(id);
        role.setName(roleRequest.name().toUpperCase());
        return new GenericResponse<>(null, String.format(AppConstant.Success.UPDATED,"Role"));
    }

    @Caching(evict = {
            @CacheEvict(value = "role-list", allEntries = true),
            @CacheEvict(value = "roles", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> delete(Long id) {
        roleRepository.delete(domainService.findRoleById(id));
        return new GenericResponse<>(null, String.format(AppConstant.Success.DELETED,"Role"));
    }


}
