package com.example.qltd.common.config;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.qltd.shared.enums.Role;
import com.example.qltd.shared.enums.UserStatus;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Profile("local")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@gmail.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .fullName("System Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin123"))
                    .phone("0900000000")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();

            userRepository.save(admin);
        }
    }
}