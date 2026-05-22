package com.devinder.loyalty.controller.user;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.response.MembershipEventResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.service.MembershipEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/me/membership-events")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "User Membership Event APIs", description = "Customer self-service endpoints to view personal subscription lifecycle logs")
public class UserMembershipEventController {

    private final MembershipEventService membershipEventService;

    @GetMapping
    @Operation(summary = "Get my membership events (paginated)", description = "Retrieve all membership events corresponding to the authenticated user's subscriptions.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token")
    })
    public ResponseEntity<ApiResponse<PageResponse<MembershipEventResponse>>> getMyEvents(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault Pageable pageable) {
        String username = userDetails.getUsername();
        log.info("REST request to list my membership events for user {}: {}", username, pageable);
        PageResponse<MembershipEventResponse> response = membershipEventService.getMyEvents(username, pageable);
        ApiResponse<PageResponse<MembershipEventResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS
        );
        return ResponseEntity.ok(apiResponse);
    }
}
