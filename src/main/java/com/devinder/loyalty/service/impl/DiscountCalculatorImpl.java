package com.devinder.loyalty.service.impl;

import com.devinder.loyalty.service.DiscountCalculator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountCalculatorImpl implements DiscountCalculator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public long calculateDiscount(long orderAmount, String benefitsJson) {
        if (benefitsJson == null || benefitsJson.isBlank() || "{}".equals(benefitsJson)) {
            return 0;
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(benefitsJson);

            // Look for DISCOUNT_PERCENTAGE benefit
            JsonNode discountNode = root.get("DISCOUNT_PERCENTAGE");
            if (discountNode != null && !discountNode.isNull()) {
                JsonNode percentageNode = discountNode.get("percentage");
                if (percentageNode != null && percentageNode.isNumber()) {
                    double percentage = percentageNode.asDouble();
                    long discount = (long) (orderAmount * percentage / 100.0);

                    // Apply maxDiscount cap if configured
                    JsonNode maxDiscountNode = discountNode.get("maxDiscount");
                    if (maxDiscountNode != null && maxDiscountNode.isNumber()) {
                        long maxDiscount = maxDiscountNode.asLong();
                        discount = Math.min(discount, maxDiscount);
                    }

                    log.info("Calculated discount: {} from {}% on order amount: {}", discount, percentage, orderAmount);
                    return discount;
                }
            }
        } catch (Exception e) {
            log.error("Failed to calculate discount from benefits JSON", e);
        }

        return 0;
    }

    @Override
    public boolean isFreeDeliveryEligible(String benefitsJson) {
        return hasBenefitWithFlag(benefitsJson, "FREE_DELIVERY", "eligible");
    }

    @Override
    public boolean isCouponEligible(String benefitsJson) {
        return hasBenefitWithFlag(benefitsJson, "COUPON_ELIGIBILITY", "eligible");
    }

    private boolean hasBenefitWithFlag(String benefitsJson, String benefitName, String flagField) {
        if (benefitsJson == null || benefitsJson.isBlank() || "{}".equals(benefitsJson)) {
            return false;
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(benefitsJson);
            JsonNode benefitNode = root.get(benefitName);
            if (benefitNode != null && !benefitNode.isNull()) {
                JsonNode flagNode = benefitNode.get(flagField);
                return flagNode != null && flagNode.isBoolean() && flagNode.asBoolean();
            }
        } catch (Exception e) {
            log.error("Failed to evaluate benefit: {} from JSON", benefitName, e);
        }

        return false;
    }
}