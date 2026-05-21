package com.devinder.loyalty.seed;

import com.devinder.loyalty.entity.User;
import com.devinder.loyalty.enums.UserRole;
import com.devinder.loyalty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSeeder {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${USER_SEED_ENABLED:false}")
    private boolean enabled;

    public void seed() {
        if (!enabled) {
            log.info("Skipping user seeding because USER_SEED_ENABLED=false");
            return;
        }

        if (userRepository.count() > 0) {
            log.info("Skipping user seeding because users already exist.");
            return;
        }

        log.info("User seeding started...");

        String defaultPasswordHash = passwordEncoder.encode("password123");

        User admin = User.builder()
                .name("Super Admin")
                .mobileNumber("769655536")
                .role(UserRole.ADMIN)
                .passwordHash(defaultPasswordHash)
                .cohort("ADMIN_COHORT")
                .build();

        List<User> users = List.of(
                admin,
                User.builder().name("Rahul Sharma").mobileNumber("769655537").role(UserRole.USER).passwordHash(defaultPasswordHash).cohort("MAY_2026").build(),
                User.builder().name("Aman Verma").mobileNumber("769655538").role(UserRole.USER).passwordHash(defaultPasswordHash).cohort("MAY_2026").build(),
                User.builder().name("Priya Singh").mobileNumber("769655539").role(UserRole.USER).passwordHash(defaultPasswordHash).cohort("MAY_2026").build(),
                User.builder().name("Neha Kapoor").mobileNumber("769655540").role(UserRole.USER).passwordHash(defaultPasswordHash).cohort("MAY_2026").build(),
                User.builder().name("Arjun Patel").mobileNumber("769655541").role(UserRole.USER).passwordHash(defaultPasswordHash).cohort("MAY_2026").build()
        );

        userRepository.saveAll(users);
        log.info("User seeding completed.");
    }
}
