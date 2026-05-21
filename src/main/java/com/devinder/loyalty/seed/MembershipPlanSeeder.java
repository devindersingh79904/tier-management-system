package com.devinder.loyalty.seed;

import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.enums.CurrencyType;
import com.devinder.loyalty.enums.DurationUnit;
import com.devinder.loyalty.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipPlanSeeder {

    private final MembershipPlanRepository membershipPlanRepository;

    @Value("${PLAN_SEED_ENABLED:false}")
    private boolean enabled;

    public void seed() {
        if (!enabled) {
            log.info("Skipping membership plan seeding because PLAN_SEED_ENABLED=false");
            return;
        }

        if (membershipPlanRepository.count() > 0) {
            log.info("Skipping membership plan seeding because plans already exist.");
            return;
        }

        log.info("Membership plan seeding started...");

        List<MembershipPlan> plans = List.of(
                MembershipPlan.builder()
                        .name("Monthly Plan")
                        .duration(1)
                        .durationUnit(DurationUnit.MONTH)
                        .basePrice(19900L) // ₹199.00
                        .currency(CurrencyType.INR)
                        .isActive(true)
                        .build(),
                MembershipPlan.builder()
                        .name("Quarterly Plan")
                        .duration(3)
                        .durationUnit(DurationUnit.MONTH)
                        .basePrice(49900L) // ₹499.00
                        .currency(CurrencyType.INR)
                        .isActive(true)
                        .build(),
                MembershipPlan.builder()
                        .name("Yearly Plan")
                        .duration(1)
                        .durationUnit(DurationUnit.YEAR)
                        .basePrice(149900L) // ₹1499.00
                        .currency(CurrencyType.INR)
                        .isActive(true)
                        .build()
        );

        membershipPlanRepository.saveAll(plans);
        log.info("Membership plan seeding completed.");
    }
}
