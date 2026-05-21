package com.devinder.loyalty.service.impl;

import com.devinder.loyalty.dto.response.UserMembershipResponse;
import com.devinder.loyalty.entity.User;
import com.devinder.loyalty.exception.ResourceNotFoundException;
import com.devinder.loyalty.mapper.UserMembershipMapper;
import com.devinder.loyalty.repository.UserMembershipRepository;
import com.devinder.loyalty.repository.UserRepository;
import com.devinder.loyalty.service.UserMembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMembershipServiceImpl implements UserMembershipService {

    private final UserMembershipRepository userMembershipRepository;
    private final UserRepository userRepository;
    private final UserMembershipMapper userMembershipMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserMembershipResponse> getMyMemberships() {
        String mobileNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching memberships for self (mobile number: {})", mobileNumber);

        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with mobile number: " + mobileNumber));

        return userMembershipRepository.findByUserId(user.getId()).stream()
                .map(userMembershipMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserMembershipResponse> getAllMemberships() {
        log.info("Fetching all customer memberships (admin only)");
        return userMembershipRepository.findAll().stream()
                .map(userMembershipMapper::toResponse)
                .collect(Collectors.toList());
    }
}
