package com.johanwork.warehouse.user.service.impl;

import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.user.entity.User;
import com.johanwork.warehouse.user.repository.UserRepository;
import com.johanwork.warehouse.user.service.IUserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDomainService implements IUserDomainService {

    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#email")
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "USER"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "User", email)));
    }

    @Cacheable(value = "users", key = "#id")
    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "USER"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "User", id)));
    }
}
