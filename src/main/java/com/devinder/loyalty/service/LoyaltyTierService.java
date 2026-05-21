package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.request.LoyaltyTierRequest;
import com.devinder.loyalty.dto.response.LoyaltyTierResponse;

public interface LoyaltyTierService {
    LoyaltyTierResponse createTier(LoyaltyTierRequest request);
    LoyaltyTierResponse getTierById(Long id);
    LoyaltyTierResponse getTierByName(String name);
    LoyaltyTierResponse updateTier(Long id, LoyaltyTierRequest request);
    void deleteTier(Long id);
}
