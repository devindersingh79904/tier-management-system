package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.request.CreateMembershipPlanRequest;
import com.devinder.loyalty.dto.response.MembershipPlanResponse;

import com.devinder.loyalty.dto.request.UpdateMembershipPlanRequest;
import com.devinder.loyalty.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MembershipPlanService {
    MembershipPlanResponse createPlan(CreateMembershipPlanRequest request);
    MembershipPlanResponse updatePlan(String id, UpdateMembershipPlanRequest request);
    MembershipPlanResponse getPlanById(String id);
    void deletePlan(String id);
    PageResponse<MembershipPlanResponse> getActivePlans(Pageable pageable);
    PageResponse<MembershipPlanResponse> getAllPlans(Pageable pageable);
    List<MembershipPlanResponse> getAllPlans();
}
