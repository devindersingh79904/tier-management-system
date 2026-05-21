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

    private static final int MONTHLY_DURATION = 1;
    private static final int QUARTERLY_DURATION = 3;
    private static final int YEARLY_DURATION = 1;

    private static final long MONTHLY_PRICE = 19900L; // ₹199.00
    private static final long QUARTERLY_PRICE = 49900L; // ₹499.00
    private static final long YEARLY_PRICE = 149900L; // ₹1499.00

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
                        .duration(MONTHLY_DURATION)
                        .durationUnit(DurationUnit.MONTH)
                        .basePrice(MONTHLY_PRICE)
                        .currency(CurrencyType.INR)
                        .isActive(true)
                        .build(),
                MembershipPlan.builder()
                        .name("Quarterly Plan")
                        .duration(QUARTERLY_DURATION)
                        .durationUnit(DurationUnit.MONTH)
                        .basePrice(QUARTERLY_PRICE)
                        .currency(CurrencyType.INR)
                        .isActive(true)
                        .build(),
                MembershipPlan.builder()
                        .name("Yearly Plan")
                        .duration(YEARLY_DURATION)
                        .durationUnit(DurationUnit.YEAR)
                        .basePrice(YEARLY_PRICE)
                        .currency(CurrencyType.INR)
                        .isActive(true)
                        .build()
        );

        membershipPlanRepository.saveAll(plans);
        log.info("Membership plan seeding completed.");
    }
}
