package com.devinder.loyalty.controller.user;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.response.UserProfileResponse;
import com.devinder.loyalty.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/me/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "User APIs", description = "Customer self-service endpoints")
public class UserProfileController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get current user profile", description = "Retrieve profile details of the authenticated user from session context.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token")
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        UserProfileResponse response = userService.getMyProfile();
        ApiResponse<UserProfileResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }
}
