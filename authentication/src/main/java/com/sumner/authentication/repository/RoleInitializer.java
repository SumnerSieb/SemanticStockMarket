package com.sumner.authentication.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.sumner.authentication.models.ERole;
import com.sumner.authentication.models.Role;
import com.sumner.authentication.repository.RoleRepository;

import java.util.Optional;

@Component
public class RoleInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        // Check and create roles if not present
        if (!roleExists(ERole.ROLE_USER)) {
            roleRepository.save(new Role(ERole.ROLE_USER));
        }

        if (!roleExists(ERole.ROLE_ADMIN)) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
        }

        if (!roleExists(ERole.ROLE_PREMIUM)) {
            roleRepository.save(new Role(ERole.ROLE_PREMIUM));
        }

        if (!roleExists(ERole.ROLE_COMMISSIONER)) {
            roleRepository.save(new Role(ERole.ROLE_COMMISSIONER));
        }
    }

    private boolean roleExists(ERole role) {
        return roleRepository.findByName(role).isPresent();
    }
}
