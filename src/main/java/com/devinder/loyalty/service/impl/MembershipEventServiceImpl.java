package com.devinder.loyalty.service.impl;

import com.devinder.loyalty.dto.response.MembershipEventResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.entity.MembershipEvent;
import com.devinder.loyalty.entity.User;
import com.devinder.loyalty.exception.ResourceNotFoundException;
import com.devinder.loyalty.mapper.MembershipEventMapper;
import com.devinder.loyalty.repository.MembershipEventRepository;
import com.devinder.loyalty.repository.UserRepository;
import com.devinder.loyalty.service.MembershipEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipEventServiceImpl implements MembershipEventService {

    private final MembershipEventRepository membershipEventRepository;
    private final UserRepository userRepository;
    private final MembershipEventMapper membershipEventMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MembershipEventResponse> getAllEvents(Pageable pageable) {
        log.info("Fetching all membership events paginated. Pageable: {}", pageable);
        Page<MembershipEvent> page = membershipEventRepository.findAll(pageable);
        return PageResponse.from(page.map(membershipEventMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipEventResponse getEventById(String id) {
        log.info("Fetching membership event by ID: {}", id);
        MembershipEvent event = membershipEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership event not found with id: " + id));
        return membershipEventMapper.toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MembershipEventResponse> getEventsByMembershipId(String membershipId, Pageable pageable) {
        log.info("Fetching membership events for membership ID: {} paginated. Pageable: {}", membershipId, pageable);
        Page<MembershipEvent> page = membershipEventRepository.findByUserMembershipId(membershipId, pageable);
        return PageResponse.from(page.map(membershipEventMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MembershipEventResponse> getMyEvents(String username, Pageable pageable) {
        log.info("Fetching membership events for user mobile: {} paginated. Pageable: {}", username, pageable);
        User user = userRepository.findByMobileNumber(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with mobile: " + username));
        Page<MembershipEvent> page = membershipEventRepository.findByUserMembershipUserId(user.getId(), pageable);
        return PageResponse.from(page.map(membershipEventMapper::toResponse));
    }
}
