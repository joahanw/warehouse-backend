package com.johanwork.warehouse.role.service;

import com.johanwork.warehouse.role.entity.Role;

public interface IRoleDomainService {
    Role findRoleByName(String name);
    Role findRoleById(Long id);
}
