package com.devinder.loyalty.controller.user;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.request.CreateUserMembershipRequest;
import com.devinder.loyalty.dto.response.UserMembershipResponse;
import com.devinder.loyalty.service.UserMembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/me/memberships")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "User Membership APIs", description = "Customer self-service endpoints for managing loyalty subscription lifecycle")
public class UserMembershipController {

    private final UserMembershipService userMembershipService;
    private final com.devinder.loyalty.service.BenefitResolverService benefitResolverService;

    @PostMapping
    @Operation(summary = "Subscribe to a membership plan", description = "Self-service customer endpoint to subscribe to a loyalty plan and tier.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Subscribed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Plan or tier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Already subscribed to an active plan or plan/tier is inactive")
    })
    public ResponseEntity<ApiResponse<UserMembershipResponse>> subscribe(
            @Valid @RequestBody CreateUserMembershipRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("REST request from user {} to subscribe: {}", username, request);
        // Force request to not bypass userId tampering
        request.setUserId(null);
        UserMembershipResponse response = userMembershipService.createMembership(request, username);
        ApiResponse<UserMembershipResponse> apiResponse = ApiResponse.success(
                response,
                "Subscribed successfully",
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get current active membership details", description = "Retrieve active membership details for the logged-in customer context.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active membership retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No active membership found")
    })
    public ResponseEntity<ApiResponse<UserMembershipResponse>> getMyActiveMembership(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("REST request to get active membership for user: {}", username);
        UserMembershipResponse response = userMembershipService.getMyActiveMembership(username);
        ApiResponse<UserMembershipResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/history")
    @Operation(summary = "Get user memberships history", description = "Retrieve list of all active and past memberships associated with the logged-in user context.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User memberships history retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token")
    })
    public ResponseEntity<ApiResponse<List<UserMembershipResponse>>> getMyMembershipsHistory(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("REST request to get memberships history for user: {}", username);
        List<UserMembershipResponse> response = userMembershipService.getMyMembershipsHistory(username);
        ApiResponse<List<UserMembershipResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/benefits")
    @Operation(summary = "Get resolved active membership benefits", description = "Retrieve merged and resolved benefits for the logged-in customer's active membership.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Benefits resolved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No active membership found")
    })
    public ResponseEntity<ApiResponse<String>> getMyResolvedBenefits(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("REST request to get resolved benefits for user: {}", username);
        UserMembershipResponse active = userMembershipService.getMyActiveMembership(username);
        String resolved = benefitResolverService.resolveBenefits(active.getMembershipPlanId(), active.getMembershipTierId());
        ApiResponse<String> apiResponse = ApiResponse.success(
                resolved,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }
}
