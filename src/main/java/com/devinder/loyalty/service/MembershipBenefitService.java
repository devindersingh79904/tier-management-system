package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.request.CreateMembershipBenefitRequest;
import com.devinder.loyalty.dto.request.UpdateMembershipBenefitRequest;
import com.devinder.loyalty.dto.response.MembershipBenefitResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface MembershipBenefitService {
    MembershipBenefitResponse createBenefit(CreateMembershipBenefitRequest request);
    MembershipBenefitResponse updateBenefit(String id, UpdateMembershipBenefitRequest request);
    MembershipBenefitResponse getBenefitById(String id);
    PageResponse<MembershipBenefitResponse> getActiveBenefits(Pageable pageable);
    PageResponse<MembershipBenefitResponse> getAllBenefits(Pageable pageable);
    void deleteBenefit(String id);
}
