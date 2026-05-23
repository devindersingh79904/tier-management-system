package com.devinder.loyalty.seed;

import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.entity.TierCriteria;
import com.devinder.loyalty.repository.MembershipTierRepository;
import com.devinder.loyalty.repository.TierCriteriaRepository;
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
public class TierCriteriaSeeder {

    private final TierCriteriaRepository tierCriteriaRepository;
    private final MembershipTierRepository membershipTierRepository;

    @Value("${TIER_CRITERIA_SEED_ENABLED:false}")
    private boolean enabled;

    public void seed() {
        if (!enabled) {
            log.info("Skipping tier criteria seeding because TIER_CRITERIA_SEED_ENABLED=false");
            return;
        }

        if (tierCriteriaRepository.count() > 0) {
            log.info("Skipping tier criteria seeding because criteria already exist.");
            return;
        }

        log.info("Tier criteria seeding started...");

        List<TierCriteria> criteriaList = new ArrayList<>();

        Optional<MembershipTier> silverOpt = membershipTierRepository.findByName("SILVER");
        silverOpt.ifPresent(tier -> {
            criteriaList.add(TierCriteria.builder()
                    .membershipTier(tier)
                    .criteriaJson("{" +
                            "\"operator\":\"AND\"," +
                            "\"rules\":[{" +
                            "\"field\":\"orderCount\"," +
                            "\"operator\":\"GREATER_THAN_OR_EQUAL\"," +
                            "\"value\":0" +
                            "}]" +
                            "}")
                    .isActive(true)
                    .build());
        });

        Optional<MembershipTier> goldOpt = membershipTierRepository.findByName("GOLD");
        goldOpt.ifPresent(tier -> {
            criteriaList.add(TierCriteria.builder()
                    .membershipTier(tier)
                    .criteriaJson("{" +
                            "\"operator\":\"AND\"," +
                            "\"rules\":[{" +
                            "\"field\":\"orderCount\"," +
                            "\"operator\":\"GREATER_THAN_OR_EQUAL\"," +
                            "\"value\":5" +
                            "},{" +
                            "\"field\":\"totalOrderValue\"," +
                            "\"operator\":\"GREATER_THAN_OR_EQUAL\"," +
                            "\"value\":150000" +
                            "}]" +
                            "}")
                    .isActive(true)
                    .build());
        });

        Optional<MembershipTier> platinumOpt = membershipTierRepository.findByName("PLATINUM");
        platinumOpt.ifPresent(tier -> {
            criteriaList.add(TierCriteria.builder()
                    .membershipTier(tier)
                    .criteriaJson("{" +
                            "\"operator\":\"AND\"," +
                            "\"rules\":[{" +
                            "\"field\":\"orderCount\"," +
                            "\"operator\":\"GREATER_THAN_OR_EQUAL\"," +
                            "\"value\":10" +
                            "},{" +
                            "\"field\":\"totalOrderValue\"," +
                            "\"operator\":\"GREATER_THAN_OR_EQUAL\"," +
                            "\"value\":500000" +
                            "}]" +
                            "}")
                    .isActive(true)
                    .build());
        });

        // Cohort-based eligibility: VIP cohort users qualify for GOLD directly
        // This uses OR logic: if cohort matches OR order criteria met
        Optional<MembershipTier> goldCohortOpt = membershipTierRepository.findByName("GOLD");
        goldCohortOpt.ifPresent(tier -> {
            criteriaList.add(TierCriteria.builder()
                    .membershipTier(tier)
                    .criteriaJson("{" +
                            "\"operator\":\"OR\"," +
                            "\"rules\":[{" +
                            "\"field\":\"cohort\"," +
                            "\"operator\":\"EQUALS\"," +
                            "\"value\":\"VIP\"" +
                            "},{" +
                            "\"field\":\"orderCount\"," +
                            "\"operator\":\"GREATER_THAN_OR_EQUAL\"," +
                            "\"value\":5" +
                            "}]" +
                            "}")
                    .isActive(true)
                    .build());
        });

        // Platinum cohort: VIP cohort with high spending qualifies for Platinum
        Optional<MembershipTier> platinumCohortOpt = membershipTierRepository.findByName("PLATINUM");
        platinumCohortOpt.ifPresent(tier -> {
            criteriaList.add(TierCriteria.builder()
                    .membershipTier(tier)
                    .criteriaJson("{" +
                            "\"operator\":\"AND\"," +
                            "\"rules\":[{" +
                            "\"field\":\"cohort\"," +
                            "\"operator\":\"EQUALS\"," +
                            "\"value\":\"VIP\"" +
                            "},{" +
                            "\"field\":\"totalOrderValue\"," +
                            "\"operator\":\"GREATER_THAN_OR_EQUAL\"," +
                            "\"value\":200000" +
                            "}]" +
                            "}")
                    .isActive(true)
                    .build());
        });

        if (!criteriaList.isEmpty()) {
            tierCriteriaRepository.saveAll(criteriaList);
        }
        log.info("Tier criteria seeding completed with {} criteria sets.", criteriaList.size());
    }
}