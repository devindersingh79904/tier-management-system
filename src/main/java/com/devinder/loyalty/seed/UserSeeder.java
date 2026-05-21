package com.devinder.loyalty.seed;

import com.devinder.loyalty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSeeder {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

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

        String hash = passwordEncoder.encode("123");
        Timestamp now = Timestamp.from(Instant.now());

        String sql = "INSERT INTO users (id, name, mobile_number, password_hash, role, cohort, created_at, updated_at, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)";

        jdbcTemplate.update(sql, "U001", "Super Admin",  "7696555536", hash, "SUPER_ADMIN", "ADMIN_COHORT", now, now);
        jdbcTemplate.update(sql, "U002", "Admin User",   "7696555537", hash, "ADMIN",       "ADMIN_COHORT", now, now);
        jdbcTemplate.update(sql, "U003", "Rahul Sharma", "7696555538", hash, "USER",        "MAY_2026",     now, now);
        jdbcTemplate.update(sql, "U004", "Aman Verma",   "7696555539", hash, "USER",        "MAY_2026",     now, now);
        jdbcTemplate.update(sql, "U005", "Priya Singh",  "7696555540", hash, "USER",        "MAY_2026",     now, now);
        jdbcTemplate.update(sql, "U006", "Neha Kapoor",  "7696555541", hash, "USER",        "MAY_2026",     now, now);
        jdbcTemplate.update(sql, "U007", "Arjun Patel",  "7696555542", hash, "USER",        "MAY_2026",     now, now);

        log.info("User seeding completed.");
    }
}
