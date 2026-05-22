package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.request.CancelMembershipRequest;
import com.devinder.loyalty.dto.request.CreateUserMembershipRequest;
import com.devinder.loyalty.dto.request.UpgradeMembershipRequest;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.dto.response.UserMembershipResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserMembershipService {
    UserMembershipResponse createMembership(CreateUserMembershipRequest request, String defaultUsername);
    UserMembershipResponse getMembershipById(String id);
    PageResponse<UserMembershipResponse> getAllMemberships(Pageable pageable);
    List<UserMembershipResponse> getAllMemberships();
    UserMembershipResponse upgradeMembership(String id, UpgradeMembershipRequest request);
    UserMembershipResponse cancelMembership(String id, CancelMembershipRequest request);
    void expireMemberships();
    List<UserMembershipResponse> getMyMembershipsHistory(String username);
    UserMembershipResponse getMyActiveMembership(String username);
    List<UserMembershipResponse> getMyMemberships(String username);
}
