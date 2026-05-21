package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.request.CreateMembershipPlanRequest;
import com.devinder.loyalty.dto.response.MembershipPlanResponse;

import java.util.List;

public interface MembershipPlanService {
    MembershipPlanResponse createPlan(CreateMembershipPlanRequest request);
    List<MembershipPlanResponse> getAllPlans();
}
