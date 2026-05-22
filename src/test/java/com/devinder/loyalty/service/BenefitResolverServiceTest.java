package com.devinder.loyalty.service;

import com.devinder.loyalty.entity.BenefitConfiguration;
import com.devinder.loyalty.entity.MembershipBenefit;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.repository.BenefitConfigurationRepository;
import com.devinder.loyalty.service.impl.BenefitResolverServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BenefitResolverServiceTest {

    @Mock
    private BenefitConfigurationRepository benefitConfigurationRepository;

    @InjectMocks
    private BenefitResolverServiceImpl benefitResolverService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private MembershipBenefit freeShippingBenefit;
    private MembershipPlan plan;
    private MembershipTier tier;

    @BeforeEach
    void setUp() {
        freeShippingBenefit = MembershipBenefit.builder().name("Free Shipping").isActive(true).build();
        freeShippingBenefit.setId("benefit-1");

        plan = MembershipPlan.builder().name("Gold Plan").isActive(true).build();
        plan.setId("plan-1");

        tier = MembershipTier.builder().name("GOLD").priority(2).isActive(true).build();
        tier.setId("tier-1");
    }

    @Test
    void resolveBenefits_NoMatchingConfigs_ReturnsEmptyObject() {
        when(benefitConfigurationRepository.findActiveConfigurations("plan-1", "tier-1"))
                .thenReturn(Collections.emptyList());

        String result = benefitResolverService.resolveBenefits("plan-1", "tier-1");

        assertEquals("{}", result);
    }

    @Test
    void resolveBenefits_SingleMatchingConfig_ReturnsCorrectJson() throws Exception {
        BenefitConfiguration config = BenefitConfiguration.builder()
                .membershipBenefit(freeShippingBenefit)
                .membershipPlan(plan)
                .configurationJson("{\"freeShippingLimit\": 500}")
                .isActive(true)
                .build();

        when(benefitConfigurationRepository.findActiveConfigurations("plan-1", "tier-1"))
                .thenReturn(Collections.singletonList(config));

        String result = benefitResolverService.resolveBenefits("plan-1", "tier-1");

        JsonNode root = OBJECT_MAPPER.readTree(result);
        assertTrue(root.has("Free Shipping"));
        assertEquals(500, root.path("Free Shipping").path("freeShippingLimit").asInt());
    }

    @Test
    void resolveBenefits_MultipleOverlappingConfigs_MergesAndOverridesCorrectly() throws Exception {
        // Plan-level config: limit = 500, courier = "Standard"
        BenefitConfiguration planConfig = BenefitConfiguration.builder()
                .membershipBenefit(freeShippingBenefit)
                .membershipPlan(plan)
                .configurationJson("{\"freeShippingLimit\": 500, \"courier\": \"Standard\"}")
                .isActive(true)
                .build();

        // Tier-level config: limit = 300 (overrides plan), speed = "Express" (adds new attribute)
        BenefitConfiguration tierConfig = BenefitConfiguration.builder()
                .membershipBenefit(freeShippingBenefit)
                .membershipTier(tier)
                .configurationJson("{\"freeShippingLimit\": 300, \"speed\": \"Express\"}")
                .isActive(true)
                .build();

        when(benefitConfigurationRepository.findActiveConfigurations("plan-1", "tier-1"))
                .thenReturn(Arrays.asList(planConfig, tierConfig));

        String result = benefitResolverService.resolveBenefits("plan-1", "tier-1");

        JsonNode root = OBJECT_MAPPER.readTree(result);
        assertTrue(root.has("Free Shipping"));

        JsonNode freeShipping = root.path("Free Shipping");
        assertEquals(300, freeShipping.path("freeShippingLimit").asInt()); // overridden by tier
        assertEquals("Standard", freeShipping.path("courier").asText()); // preserved from plan
        assertEquals("Express", freeShipping.path("speed").asText()); // added from tier
    }
}
