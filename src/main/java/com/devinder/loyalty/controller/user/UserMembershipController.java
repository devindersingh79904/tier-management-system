package com.devinder.loyalty.controller.user;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.request.CancelMembershipRequest;
import com.devinder.loyalty.dto.request.CreateUserMembershipRequest;
import com.devinder.loyalty.dto.request.DowngradeMembershipRequest;
import com.devinder.loyalty.dto.request.UpgradeMembershipRequest;
import com.devinder.loyalty.dto.response.UserMembershipResponse;
import com.devinder.loyalty.service.BenefitResolverService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final BenefitResolverService benefitResolverService;

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

    @PutMapping("/{membershipId}/upgrade")
    @Operation(summary = "Upgrade membership tier", description = "Upgrade the active membership to a higher tier. Only the owning user can upgrade.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Membership upgraded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership or target tier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cannot upgrade to a lower/equal priority tier or membership is not active")
    })
    public ResponseEntity<ApiResponse<UserMembershipResponse>> upgradeMembership(
            @PathVariable String membershipId,
            @Valid @RequestBody UpgradeMembershipRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("REST request from user {} to upgrade membership: {}", username, membershipId);

        // Validate ownership before proceeding
        UserMembershipResponse active = userMembershipService.getMyActiveMembership(username);
        if (!active.getId().equals(membershipId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You can only upgrade your own active membership", "Forbidden", HttpStatus.FORBIDDEN.value()));
        }

        UserMembershipResponse response = userMembershipService.upgradeMembership(membershipId, request);
        ApiResponse<UserMembershipResponse> apiResponse = ApiResponse.success(
                response,
                "Membership upgraded successfully",
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{membershipId}/downgrade")
    @Operation(summary = "Downgrade membership tier", description = "Downgrade the active membership to a lower tier. Only the owning user can downgrade.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Membership downgraded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership or target tier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cannot downgrade to a higher/equal priority tier or membership is not active")
    })
    public ResponseEntity<ApiResponse<UserMembershipResponse>> downgradeMembership(
            @PathVariable String membershipId,
            @Valid @RequestBody DowngradeMembershipRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("REST request from user {} to downgrade membership: {}", username, membershipId);

        // Validate ownership before proceeding
        UserMembershipResponse active = userMembershipService.getMyActiveMembership(username);
        if (!active.getId().equals(membershipId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You can only downgrade your own active membership", "Forbidden", HttpStatus.FORBIDDEN.value()));
        }

        UserMembershipResponse response = userMembershipService.downgradeMembership(membershipId, request);
        ApiResponse<UserMembershipResponse> apiResponse = ApiResponse.success(
                response,
                "Membership downgraded successfully",
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{membershipId}/cancel")
    @Operation(summary = "Cancel membership", description = "Cancel the active membership. Only the owning user can cancel.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Membership cancelled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cannot cancel a membership that is not active")
    })
    public ResponseEntity<ApiResponse<UserMembershipResponse>> cancelMembership(
            @PathVariable String membershipId,
            @Valid @RequestBody CancelMembershipRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("REST request from user {} to cancel membership: {}", username, membershipId);

        // Validate ownership before proceeding
        UserMembershipResponse active = userMembershipService.getMyActiveMembership(username);
        if (!active.getId().equals(membershipId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You can only cancel your own active membership", "Forbidden", HttpStatus.FORBIDDEN.value()));
        }

        UserMembershipResponse response = userMembershipService.cancelMembership(membershipId, request);
        ApiResponse<UserMembershipResponse> apiResponse = ApiResponse.success(
                response,
                "Membership cancelled successfully",
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }
}