package com.devinder.loyalty.seed;

import com.devinder.loyalty.entity.BenefitConfiguration;
import com.devinder.loyalty.entity.MembershipBenefit;
import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.repository.BenefitConfigurationRepository;
import com.devinder.loyalty.repository.MembershipBenefitRepository;
import com.devinder.loyalty.repository.MembershipTierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BenefitConfigurationSeeder {

    private final BenefitConfigurationRepository benefitConfigurationRepository;
    private final MembershipBenefitRepository membershipBenefitRepository;
    private final MembershipTierRepository membershipTierRepository;

    @Value("${BENEFIT_CONFIGURATION_SEED_ENABLED:false}")
    private boolean enabled;

    public void seed() {
        if (!enabled) {
            log.info("Skipping benefit configuration seeding because BENEFIT_CONFIGURATION_SEED_ENABLED=false");
            return;
        }

        if (benefitConfigurationRepository.count() > 0) {
            log.info("Skipping benefit configuration seeding because configurations already exist.");
            return;
        }

        log.info("Benefit configuration seeding started...");

        List<BenefitConfiguration> configs = new ArrayList<>();

        Optional<MembershipBenefit> freeDeliveryOpt = membershipBenefitRepository.findByName("FREE_DELIVERY");
        Optional<MembershipBenefit> prioritySupportOpt = membershipBenefitRepository.findByName("PRIORITY_SUPPORT");
        Optional<MembershipBenefit> earlyAccessOpt = membershipBenefitRepository.findByName("EARLY_ACCESS");
        Optional<MembershipBenefit> extraDiscountOpt = membershipBenefitRepository.findByName("EXTRA_DISCOUNT");

        Optional<MembershipTier> goldTier = membershipTierRepository.findByName("GOLD");
        Optional<MembershipTier> platinumTier = membershipTierRepository.findByName("PLATINUM");

        // FREE_DELIVERY for Gold & Platinum
        freeDeliveryOpt.ifPresent(benefit -> {
            goldTier.ifPresent(tier -> configs.add(BenefitConfiguration.builder()
                    .membershipBenefit(benefit)
                    .membershipTier(tier)
                    .configurationJson("{\"enabled\":true}")
                    .isActive(true)
                    .build()));
            platinumTier.ifPresent(tier -> configs.add(BenefitConfiguration.builder()
                    .membershipBenefit(benefit)
                    .membershipTier(tier)
                    .configurationJson("{\"enabled\":true}")
                    .isActive(true)
                    .build()));
        });

        // EXTRA_DISCOUNT for Gold (10%) & Platinum (15%)
        extraDiscountOpt.ifPresent(benefit -> {
            goldTier.ifPresent(tier -> configs.add(BenefitConfiguration.builder()
                    .membershipBenefit(benefit)
                    .membershipTier(tier)
                    .configurationJson("{\"discount_percent\":10,\"max_discount_paise\":50000}")
                    .isActive(true)
                    .build()));
            platinumTier.ifPresent(tier -> configs.add(BenefitConfiguration.builder()
                    .membershipBenefit(benefit)
                    .membershipTier(tier)
                    .configurationJson("{\"discount_percent\":15,\"max_discount_paise\":100000}")
                    .isActive(true)
                    .build()));
        });

        // PRIORITY_SUPPORT and EARLY_ACCESS for Platinum
        prioritySupportOpt.ifPresent(benefit -> platinumTier.ifPresent(tier -> configs.add(BenefitConfiguration.builder()
                .membershipBenefit(benefit)
                .membershipTier(tier)
                .configurationJson("{\"enabled\":true}")
                .isActive(true)
                .build())));

        earlyAccessOpt.ifPresent(benefit -> platinumTier.ifPresent(tier -> configs.add(BenefitConfiguration.builder()
                .membershipBenefit(benefit)
                .membershipTier(tier)
                .configurationJson("{\"enabled\":true}")
                .isActive(true)
                .build())));

        if (!configs.isEmpty()) {
            benefitConfigurationRepository.saveAll(configs);
        }
        log.info("Benefit configuration seeding completed.");
    }
}
