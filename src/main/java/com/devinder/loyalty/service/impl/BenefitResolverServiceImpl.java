package com.devinder.loyalty.service.impl;

import com.devinder.loyalty.entity.BenefitConfiguration;
import com.devinder.loyalty.repository.BenefitConfigurationRepository;
import com.devinder.loyalty.service.BenefitResolverService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BenefitResolverServiceImpl implements BenefitResolverService {

    private final BenefitConfigurationRepository benefitConfigurationRepository;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public String resolveBenefits(String membershipPlanId, String membershipTierId) {
        log.info("Resolving benefits for plan: {} and tier: {}", membershipPlanId, membershipTierId);

        if (membershipPlanId == null && membershipTierId == null) {
            return "{}";
        }

        // Fetch active benefit configurations matching either the plan or the tier (or both)
        List<BenefitConfiguration> configs = benefitConfigurationRepository.findActiveConfigurations(
                membershipPlanId, membershipTierId);

        // Group configurations by benefit name
        Map<String, List<BenefitConfiguration>> grouped = configs.stream()
                .collect(Collectors.groupingBy(c -> c.getMembershipBenefit().getName()));

        ObjectNode resultNode = OBJECT_MAPPER.createObjectNode();

        for (Map.Entry<String, List<BenefitConfiguration>> entry : grouped.entrySet()) {
            String benefitName = entry.getKey();
            List<BenefitConfiguration> benefitConfigs = entry.getValue();

            // Sort benefit configurations by specificity:
            // 1. Plan only (lowest specificity)
            // 2. Tier only (medium specificity)
            // 3. Both Plan & Tier (highest specificity)
            benefitConfigs.sort(Comparator.comparingInt(this::getSpecificityScore));

            ObjectNode mergedBenefitConfig = OBJECT_MAPPER.createObjectNode();
            for (BenefitConfiguration config : benefitConfigs) {
                try {
                    JsonNode node = OBJECT_MAPPER.readTree(config.getConfigurationJson());
                    if (node.isObject()) {
                        mergeJsonNodes(mergedBenefitConfig, node);
                    } else {
                        mergedBenefitConfig.set("value", node);
                    }
                } catch (Exception e) {
                    log.error("Failed to parse configuration JSON for benefit: {}", benefitName, e);
                }
            }
            resultNode.set(benefitName, mergedBenefitConfig);
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(resultNode);
        } catch (Exception e) {
            log.error("Failed to serialize resolved benefits to JSON string", e);
            return "{}";
        }
    }

    private int getSpecificityScore(BenefitConfiguration config) {
        boolean hasPlan = config.getMembershipPlan() != null;
        boolean hasTier = config.getMembershipTier() != null;
        if (hasPlan && hasTier) {
            return 3;
        } else if (hasTier) {
            return 2;
        } else {
            return 1;
        }
    }

    private void mergeJsonNodes(ObjectNode mainNode, JsonNode updateNode) {
        updateNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            if (mainNode.has(key) && mainNode.get(key).isObject() && value.isObject()) {
                mergeJsonNodes((ObjectNode) mainNode.get(key), value);
            } else {
                mainNode.set(key, value.deepCopy());
            }
        });
    }
}
