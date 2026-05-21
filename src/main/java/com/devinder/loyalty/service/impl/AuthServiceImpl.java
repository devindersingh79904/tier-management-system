package com.devinder.loyalty.service.impl;

import com.devinder.loyalty.constants.SecurityConstants;
import com.devinder.loyalty.dto.request.LoginRequest;
import com.devinder.loyalty.dto.request.SignupRequest;
import com.devinder.loyalty.dto.response.AuthResponse;
import com.devinder.loyalty.entity.User;
import com.devinder.loyalty.enums.UserRole;
import com.devinder.loyalty.exception.ConflictException;
import com.devinder.loyalty.exception.UnauthorizedException;
import com.devinder.loyalty.repository.UserRepository;
import com.devinder.loyalty.service.AuthService;
import com.devinder.loyalty.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.findByMobileNumber(request.getMobileNumber()).isPresent()) {
            throw new ConflictException(SecurityConstants.DUPLICATE_MOBILE);
        }

        User user = User.builder()
                .name(request.getName())
                .mobileNumber(request.getMobileNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .build();

        user = userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getMobileNumber(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getMobileNumber());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(SecurityConstants.TOKEN_TYPE)
                .accessTokenExpiresIn(jwtUtil.getAccessExpirySeconds())
                .refreshTokenExpiresIn(jwtUtil.getRefreshExpirySeconds())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByMobileNumber(request.getMobileNumber())
                .orElseThrow(() -> new UnauthorizedException(SecurityConstants.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException(SecurityConstants.INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getMobileNumber(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getMobileNumber());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(SecurityConstants.TOKEN_TYPE)
                .accessTokenExpiresIn(jwtUtil.getAccessExpirySeconds())
                .refreshTokenExpiresIn(jwtUtil.getRefreshExpirySeconds())
                .build();
    }
}
