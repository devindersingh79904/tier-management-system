package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.response.MembershipEventResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface MembershipEventService {
    PageResponse<MembershipEventResponse> getAllEvents(Pageable pageable);
    MembershipEventResponse getEventById(String id);
    PageResponse<MembershipEventResponse> getEventsByMembershipId(String membershipId, Pageable pageable);
    PageResponse<MembershipEventResponse> getMyEvents(String username, Pageable pageable);
}
