package com.johanwork.warehouse.user.service.impl;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.role.entity.Role;
import com.johanwork.warehouse.role.service.IRoleDomainService;
import com.johanwork.warehouse.user.dto.UserRequest;
import com.johanwork.warehouse.user.dto.UserResponse;
import com.johanwork.warehouse.user.dto.UserRoleResponse;
import com.johanwork.warehouse.user.entity.User;
import com.johanwork.warehouse.user.mapper.UserMapper;
import com.johanwork.warehouse.user.repository.UserRepository;
import com.johanwork.warehouse.user.service.IUserDomainService;
import com.johanwork.warehouse.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final IRoleDomainService roleService;
    private final PasswordEncoder passwordEncoder;
    private final CompromisedPasswordChecker compromisedPasswordChecker;
    private final IUserDomainService domainService;

    @Cacheable(
            value = "user-list",
            condition = "#search == null || #search.isBlank()",
            key = "T(String).format('%d-%d-%s-%s', #pageNumber, #pageSize, #sortBy, #sortDirection)"
    )
    @Override
    public GenericResponse<PageResponse<UserResponse>> getAllUsers(int pageNumber, int pageSize,
                                                                   String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<User> content = userRepository.findAll(pageable);
        return userMapper.mapToPageGenericResponse(content, String.format(AppConstant.Success.FETCHED,"User"));
    }

    @Override
    public GenericResponse<UserResponse> getUserById(Long id) {
        return userMapper.mapToGenericResponse(domainService.findById(id), String.format(AppConstant.Success.FETCHED,"User"));
    }

    @Caching(evict = {
            @CacheEvict(value = "user-list", allEntries = true),
            @CacheEvict(value = "users", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> create(UserRequest userRequest) {
        validateUser(userRequest);
        User user = userMapper.mapRequestToEntity(userRequest);
        Role role = roleService.findRoleByName(AppConstant.Role.USER);
        user.getRoles().add(role);
        user.setPassword(passwordEncoder.encode(userRequest.password()));
        userRepository.save(user);
        return userMapper.mapToGenericResponse(String.format(AppConstant.Success.CREATED, "User"));
    }

    @Caching(evict = {
            @CacheEvict(value = "user-list", allEntries = true),
            @CacheEvict(value = "users", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> update(Long id, UserRequest userRequest) {
        User user = domainService.findById(id);
        User updated = userMapper.mapRequestToEntity(userRequest);
        if (null == userRequest.password()){
            updated.setPassword(user.getPassword());
        }
        updated.setId(id);
        updated.setRoles(user.getRoles());
        userRepository.save(updated);
        return userMapper.mapToGenericResponse(String.format(AppConstant.Success.UPDATED, "User"));
    }

    @Caching(evict = {
            @CacheEvict(value = "user-list", allEntries = true),
            @CacheEvict(value = "users", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> delete(Long id) {
        userRepository.delete(domainService.findById(id));
        return userMapper.mapToGenericResponse(String.format(AppConstant.Success.DELETED, "User"));
    }

    @Override
    public GenericResponse<List<UserRoleResponse>> getUserByRoleName(String roleName) {
        List<User> users = userRepository.getUserByRolesName(roleName);
        return userMapper.mapToListUserRoleResponse(users, String.format(AppConstant.Success.FETCHED,"User Roles"));
    }

    @Override
    public GenericResponse<PageResponse<UserRoleResponse>> getAllUserRole(int pageNumber, int pageSize,
                                                                          String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        return userMapper.mapToPageUserRoleResponse(
                userRepository.getAllUserRoles(pageable),
                String.format(AppConstant.Success.FETCHED, "User Roles")
        ) ;
    }

    @Caching(evict = {
            @CacheEvict(value = "role-list", allEntries = true),
            @CacheEvict(value = "roles", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> assignUserToRole(Long userId, Long roleId) {
        User user = domainService.findById(userId);
        user.getRoles().add(roleService.findRoleById(roleId));
        return userMapper.mapToGenericResponse(String.format(AppConstant.Success.CREATED, "User Roles"));
    }

    @Caching(evict = {
            @CacheEvict(value = "role-list", allEntries = true),
            @CacheEvict(value = "roles", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> updateUserRole(Long userId, Long roleId, Long assignedRoleId) {
        User user = domainService.findById(userId);
        user.getRoles().remove(roleService.findRoleById(roleId));
        user.getRoles().add(roleService.findRoleById(assignedRoleId));
        return userMapper.mapToGenericResponse(String.format(AppConstant.Success.UPDATED, "User Roles"));
    }

    private void validateUser(UserRequest userRequest) {
        Optional<User> existsUser = userRepository.findByEmailOrPhone(
                userRequest.email(), userRequest.phone());
        Map<String, String> violation = new HashMap<>();
        if (existsUser.isPresent()){
            User user = existsUser.get();
            if (userRequest.email().equalsIgnoreCase(user.getEmail())) violation.put("email","Email already exists");
            if (userRequest.phone().equalsIgnoreCase(user.getPhone())) violation.put("phone","Phone already exists");
        }
        var decision = compromisedPasswordChecker.check(userRequest.password());
        if (decision.isCompromised()) violation.put("password", "Password compromised, choose a stronger password");
        if (!violation.isEmpty()) throw new CustomException(HttpStatus.BAD_REQUEST,
                String.format(AppConstant.Error.TITLE_BAD_REQUEST, "User"),
                String.format(AppConstant.Error.MESSAGE_BAD_REQUEST, "User", violation),
                violation);
    }

}
