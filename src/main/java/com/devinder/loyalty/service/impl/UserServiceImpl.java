package com.devinder.loyalty.service.impl;

import com.devinder.loyalty.dto.response.UserProfileResponse;
import com.devinder.loyalty.entity.User;
import com.devinder.loyalty.exception.ResourceNotFoundException;
import com.devinder.loyalty.mapper.UserMapper;
import com.devinder.loyalty.repository.UserRepository;
import com.devinder.loyalty.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        String mobileNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching profile for user with mobile number: {}", mobileNumber);

        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with mobile number: " + mobileNumber));

        return userMapper.toResponse(user);
    }
}
