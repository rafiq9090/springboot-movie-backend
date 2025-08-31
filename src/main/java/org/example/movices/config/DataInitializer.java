package org.example.movices.config;

import lombok.RequiredArgsConstructor;
import org.example.movices.model.entity.Role;
import org.example.movices.model.entity.enums.RoleType;
import org.example.movices.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.findByName(RoleType.ROLE_USER).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(RoleType.ROLE_USER);
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName(RoleType.ROLE_ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(RoleType.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }
    }
}
