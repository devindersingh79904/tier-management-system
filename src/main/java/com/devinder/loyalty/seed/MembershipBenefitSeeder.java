package com.devinder.loyalty.seed;

import com.devinder.loyalty.entity.MembershipBenefit;
import com.devinder.loyalty.repository.MembershipBenefitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipBenefitSeeder {

    private final MembershipBenefitRepository membershipBenefitRepository;

    @Value("${BENEFIT_SEED_ENABLED:false}")
    private boolean enabled;

    public void seed() {
        if (!enabled) {
            log.info("Skipping membership benefit seeding because BENEFIT_SEED_ENABLED=false");
            return;
        }

        if (membershipBenefitRepository.count() > 0) {
            log.info("Skipping membership benefit seeding because benefits already exist.");
            return;
        }

        log.info("Membership benefit seeding started...");

        List<MembershipBenefit> benefits = List.of(
                MembershipBenefit.builder()
                        .name("FREE_DELIVERY")
                        .description("No delivery fees on orders above a dynamic threshold.")
                        .isActive(true)
                        .build(),
                MembershipBenefit.builder()
                        .name("PRIORITY_SUPPORT")
                        .description("24/7 dedicated support phone line and rapid chat response times.")
                        .isActive(true)
                        .build(),
                MembershipBenefit.builder()
                        .name("EARLY_ACCESS")
                        .description("First access to exclusive product launches and seasonal sales.")
                        .isActive(true)
                        .build(),
                MembershipBenefit.builder()
                        .name("EXTRA_DISCOUNT")
                        .description("Additional percentage discounts applied on checkouts.")
                        .isActive(true)
                        .build()
        );

        membershipBenefitRepository.saveAll(benefits);
        log.info("Membership benefit seeding completed.");
    }
}
