package com.devinder.loyalty.seed;

import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.repository.MembershipTierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipTierSeeder {

    private final MembershipTierRepository membershipTierRepository;

    @Value("${TIER_SEED_ENABLED:false}")
    private boolean enabled;

    public void seed() {
        if (!enabled) {
            log.info("Skipping membership tier seeding because TIER_SEED_ENABLED=false");
            return;
        }

        if (membershipTierRepository.count() > 0) {
            log.info("Skipping membership tier seeding because tiers already exist.");
            return;
        }

        log.info("Membership tier seeding started...");

        List<MembershipTier> tiers = List.of(
                MembershipTier.builder()
                        .name("SILVER")
                        .priority(1)
                        .description("Standard benefits for entry-level loyalty members.")
                        .isActive(true)
                        .build(),
                MembershipTier.builder()
                        .name("GOLD")
                        .priority(2)
                        .description("Enhanced benefits and exclusive reward privileges.")
                        .isActive(true)
                        .build(),
                MembershipTier.builder()
                        .name("PLATINUM")
                        .priority(3)
                        .description("Top-tier premium rewards, dedicated support, and early access.")
                        .isActive(true)
                        .build()
        );

        membershipTierRepository.saveAll(tiers);
        log.info("Membership tier seeding completed.");
    }
}
