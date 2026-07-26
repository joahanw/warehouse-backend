package com.johanwork.warehouse.common.seeder;

import com.johanwork.warehouse.role.entity.Role;
import com.johanwork.warehouse.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(1)
@Slf4j
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count()>0){
            return;
        }
        List<Role> roles = List.of(
                new Role(null, "MANAGER", null),
                new Role(null, "KEEPER",null),
                new Role(null, "USER", null)
        );
        roleRepository.saveAll(roles);
       log.info("✅Default roles created.");
    }
}
