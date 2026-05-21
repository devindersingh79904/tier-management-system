package com.devinder.loyalty.service;

import com.devinder.loyalty.dto.request.LoginRequest;
import com.devinder.loyalty.dto.request.SignupRequest;
import com.devinder.loyalty.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
}
