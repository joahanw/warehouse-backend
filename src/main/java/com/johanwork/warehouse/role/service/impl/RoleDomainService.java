package com.johanwork.warehouse.role.service.impl;

import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.role.entity.Role;
import com.johanwork.warehouse.role.repository.RoleRepository;
import com.johanwork.warehouse.role.service.IRoleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleDomainService implements IRoleDomainService {

    private final RoleRepository roleRepository;

    @Cacheable(value = "roles", key = "#name")
    @Override
    public Role findRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "ROLE"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Role", name)));
    }

    @Cacheable(value = "roles", key = "#id")
    @Override
    public Role findRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "ROLE"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Role", id)));
    }
}
