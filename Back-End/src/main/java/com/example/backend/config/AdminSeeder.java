package com.example.backend.config;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.entity.UserStatus;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first administrator on start-up when the database has no admin yet.
 * Without this there would be no way to approve the very first registrations.
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.full-name:Site Administrator}")
    private String adminFullName;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {

        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        if (adminEmail.isBlank()
                || adminPassword.length() < 8
                || adminPassword.startsWith("CHANGE_ME")) {
            log.warn("No administrator exists yet. Set app.admin.email and app.admin.password "
                    + "in secrets.properties, then restart to create one.");
            return;
        }

        User admin = new User();
        admin.setFullName(adminFullName);
        admin.setEmail(UserService.normalizeEmail(adminEmail));
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.APPROVED);
        userRepository.save(admin);

        log.info("First administrator created: {}", admin.getEmail());
    }
}
