package com.johanwork.warehouse.user.repository;

import com.johanwork.warehouse.user.dto.UserResponse;
import com.johanwork.warehouse.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailOrPhone(String email, String phone);

    Page<User> getAllUserRoles(Pageable pageable);
    List<User> getUserByRolesName(@Param("roleName")String name);
}
