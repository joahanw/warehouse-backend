package com.johanwork.warehouse.user.service;

import com.johanwork.warehouse.user.entity.User;

public interface IUserDomainService {
    User findByEmail(String email);
    User findById(Long id);
}
