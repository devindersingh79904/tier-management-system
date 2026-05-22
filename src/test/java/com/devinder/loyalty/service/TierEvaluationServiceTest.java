package com.devinder.loyalty.service;

import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.entity.TierCriteria;
import com.devinder.loyalty.repository.TierCriteriaRepository;
import com.devinder.loyalty.service.impl.TierEvaluationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TierEvaluationServiceTest {

    @Mock
    private TierCriteriaRepository tierCriteriaRepository;

    @InjectMocks
    private TierEvaluationServiceImpl tierEvaluationService;

    private MembershipTier tier;
    private TierCriteria criteria;
    private Map<String, Object> context;

    @BeforeEach
    void setUp() {
        tier = MembershipTier.builder().name("GOLD").priority(2).isActive(true).build();
        tier.setId("tier-123");

        criteria = TierCriteria.builder()
                .membershipTier(tier)
                .isActive(true)
                .build();

        context = new HashMap<>();
    }

    @Test
    void evaluateEligibility_NoActiveCriteria_ReturnsFalse() {
        when(tierCriteriaRepository.findByMembershipTierIdAndIsActiveTrue("tier-123"))
                .thenReturn(Optional.empty());

        boolean eligible = tierEvaluationService.evaluateEligibility("tier-123", context);

        assertFalse(eligible);
    }

    @Test
    void evaluateEligibility_SimpleEqualsRule_Success() {
        criteria.setCriteriaJson("{\"field\": \"isVip\", \"operator\": \"EQUALS\", \"value\": true}");
        when(tierCriteriaRepository.findByMembershipTierIdAndIsActiveTrue("tier-123"))
                .thenReturn(Optional.of(criteria));

        context.put("isVip", true);
        assertTrue(tierEvaluationService.evaluateEligibility("tier-123", context));

        context.put("isVip", false);
        assertFalse(tierEvaluationService.evaluateEligibility("tier-123", context));
    }

    @Test
    void evaluateEligibility_NumericGreaterThanRule_Success() {
        criteria.setCriteriaJson("{\"field\": \"totalSpent\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 10000}");
        when(tierCriteriaRepository.findByMembershipTierIdAndIsActiveTrue("tier-123"))
                .thenReturn(Optional.of(criteria));

        context.put("totalSpent", 12000L);
        assertTrue(tierEvaluationService.evaluateEligibility("tier-123", context));

        context.put("totalSpent", 8000L);
        assertFalse(tierEvaluationService.evaluateEligibility("tier-123", context));
    }

    @Test
    void evaluateEligibility_CompoundAndRule_Success() {
        criteria.setCriteriaJson("{" +
                "  \"operator\": \"AND\"," +
                "  \"rules\": [" +
                "    {\"field\": \"totalSpent\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 10000}," +
                "    {\"field\": \"totalOrders\", \"operator\": \"GREATER_THAN\", \"value\": 5}" +
                "  ]" +
                "}");
        when(tierCriteriaRepository.findByMembershipTierIdAndIsActiveTrue("tier-123"))
                .thenReturn(Optional.of(criteria));

        context.put("totalSpent", 12000L);
        context.put("totalOrders", 6);
        assertTrue(tierEvaluationService.evaluateEligibility("tier-123", context));

        context.put("totalSpent", 12000L);
        context.put("totalOrders", 4);
        assertFalse(tierEvaluationService.evaluateEligibility("tier-123", context));
    }

    @Test
    void evaluateEligibility_NestedOrRule_Success() {
        criteria.setCriteriaJson("{" +
                "  \"operator\": \"OR\"," +
                "  \"rules\": [" +
                "    {\"field\": \"isVip\", \"operator\": \"EQUALS\", \"value\": true}," +
                "    {" +
                "      \"operator\": \"AND\"," +
                "      \"rules\": [" +
                "        {\"field\": \"totalSpent\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 10000}," +
                "        {\"field\": \"totalOrders\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 5}" +
                "      ]" +
                "    }" +
                "  ]" +
                "}");
        when(tierCriteriaRepository.findByMembershipTierIdAndIsActiveTrue("tier-123"))
                .thenReturn(Optional.of(criteria));

        // VIP only
        context.put("isVip", true);
        context.put("totalSpent", 0L);
        context.put("totalOrders", 0);
        assertTrue(tierEvaluationService.evaluateEligibility("tier-123", context));

        // Spent + Orders, non VIP
        context.put("isVip", false);
        context.put("totalSpent", 12000L);
        context.put("totalOrders", 6);
        assertTrue(tierEvaluationService.evaluateEligibility("tier-123", context));

        // Neither
        context.put("isVip", false);
        context.put("totalSpent", 8000L);
        context.put("totalOrders", 6);
        assertFalse(tierEvaluationService.evaluateEligibility("tier-123", context));
    }
}
