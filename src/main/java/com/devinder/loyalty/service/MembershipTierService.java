package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.request.CreateMembershipTierRequest;
import com.devinder.loyalty.dto.request.UpdateMembershipTierRequest;
import com.devinder.loyalty.dto.response.MembershipTierResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface MembershipTierService {
    MembershipTierResponse createTier(CreateMembershipTierRequest request);
    MembershipTierResponse updateTier(String id, UpdateMembershipTierRequest request);
    MembershipTierResponse getTierById(String id);
    PageResponse<MembershipTierResponse> getAllTiers(Pageable pageable);
    void deleteTier(String id);
}
