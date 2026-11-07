package com.sakcadag.security;

import com.sakcadag.entity.Role;
import com.sakcadag.entity.User;
import com.sakcadag.respository.RoleRepository;
import com.sakcadag.respository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class DataInitializer implements ApplicationRunner {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Value("${app.admin.username}")
    private String adminUsername;
    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role adminRole = findOrCreateRole("ADMIN");
        findOrCreateRole("CASHIER");

        if (userRepository.findByUsername(adminUsername).isPresent()) {
            return;
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("No initial admin user created: app.admin.password is not set");
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRoles(new HashSet<>(Set.of(adminRole)));
        userRepository.save(admin);
        log.info("Created initial admin user '{}'", adminUsername);
    }

    private Role findOrCreateRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            return roleRepository.save(role);
        });
    }
}
