package com.devinder.loyalty.service;

/**
 * Service for calculating discounts based on resolved membership benefit configurations.
 */
public interface DiscountCalculator {

    /**
     * Calculate the discount amount for a given order total based on membership benefits.
     *
     * @param orderAmount   the original order amount in paise/cents
     * @param benefitsJson  the resolved benefits JSON string from BenefitResolverService
     * @return the calculated discount amount in paise/cents
     */
    long calculateDiscount(long orderAmount, String benefitsJson);

    /**
     * Check if the membership provides free delivery eligibility.
     *
     * @param benefitsJson the resolved benefits JSON string from BenefitResolverService
     * @return true if free delivery is applicable
     */
    boolean isFreeDeliveryEligible(String benefitsJson);

    /**
     * Check if the membership provides coupon eligibility.
     *
     * @param benefitsJson the resolved benefits JSON string from BenefitResolverService
     * @return true if coupon usage is allowed
     */
    boolean isCouponEligible(String benefitsJson);
}