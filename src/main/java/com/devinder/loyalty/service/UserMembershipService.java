package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.response.UserMembershipResponse;

import java.util.List;

public interface UserMembershipService {
    List<UserMembershipResponse> getMyMemberships();
    List<UserMembershipResponse> getAllMemberships();
}
