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
                    .criteriaJson("{\"operator\":\"AND\",\"rules\":[{\"field\":\"min_orders\",\"condition\":\">=\",\"value\":0}]}")
                    .isActive(true)
                    .build());
        });

        Optional<MembershipTier> goldOpt = membershipTierRepository.findByName("GOLD");
        goldOpt.ifPresent(tier -> {
            criteriaList.add(TierCriteria.builder()
                    .membershipTier(tier)
                    .criteriaJson("{\"operator\":\"AND\",\"rules\":[{\"field\":\"min_orders\",\"condition\":\">=\",\"value\":5},{\"field\":\"min_spend\",\"condition\":\">=\",\"value\":150000}]}")
                    .isActive(true)
                    .build());
        });

        Optional<MembershipTier> platinumOpt = membershipTierRepository.findByName("PLATINUM");
        platinumOpt.ifPresent(tier -> {
            criteriaList.add(TierCriteria.builder()
                    .membershipTier(tier)
                    .criteriaJson("{\"operator\":\"AND\",\"rules\":[{\"field\":\"min_orders\",\"condition\":\">=\",\"value\":10},{\"field\":\"min_spend\",\"condition\":\">=\",\"value\":500000}]}")
                    .isActive(true)
                    .build());
        });

        if (!criteriaList.isEmpty()) {
            tierCriteriaRepository.saveAll(criteriaList);
        }
        log.info("Tier criteria seeding completed.");
    }
}
