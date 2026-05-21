package com.devinder.loyalty.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserSeeder userSeeder;
    private final MembershipTierSeeder membershipTierSeeder;
    private final MembershipPlanSeeder membershipPlanSeeder;
    private final MembershipBenefitSeeder membershipBenefitSeeder;
    private final TierCriteriaSeeder tierCriteriaSeeder;
    private final BenefitConfigurationSeeder benefitConfigurationSeeder;
    private final UserMembershipSeeder userMembershipSeeder;
    private final PaymentIntentSeeder paymentIntentSeeder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${GLOBAL_SEED_ENABLED:false}")
    private boolean globalSeedEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (!globalSeedEnabled) {
            log.info("Global seeding is disabled (GLOBAL_SEED_ENABLED=false).");
            return;
        }

        log.info("Starting global data seeding process...");
        try {
            cleanDatabase();
            userSeeder.seed();
            membershipTierSeeder.seed();
            membershipPlanSeeder.seed();
            membershipBenefitSeeder.seed();
            tierCriteriaSeeder.seed();
            benefitConfigurationSeeder.seed();
            userMembershipSeeder.seed();
            paymentIntentSeeder.seed();
            log.info("Global data seeding completed successfully.");
        } catch (Exception e) {
            log.error("An error occurred during global data seeding: ", e);
            throw e;
        }
    }

    private void cleanDatabase() {
        log.info("Cleaning up database before seeding...");
        jdbcTemplate.execute("DELETE FROM payment_intents");
        jdbcTemplate.execute("DELETE FROM membership_events");
        jdbcTemplate.execute("DELETE FROM user_memberships");
        jdbcTemplate.execute("DELETE FROM benefit_configurations");
        jdbcTemplate.execute("DELETE FROM tier_criteria");
        jdbcTemplate.execute("DELETE FROM membership_benefits");
        jdbcTemplate.execute("DELETE FROM membership_plans");
        jdbcTemplate.execute("DELETE FROM membership_tiers");
        jdbcTemplate.execute("DELETE FROM users");
        log.info("Database cleaned successfully.");
    }
}
