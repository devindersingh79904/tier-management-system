package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.request.CancelMembershipRequest;
import com.devinder.loyalty.dto.request.CreateUserMembershipRequest;
import com.devinder.loyalty.dto.request.UpgradeMembershipRequest;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.dto.response.UserMembershipResponse;
import com.devinder.loyalty.service.UserMembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/memberships")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Tag(name = "Admin Membership APIs", description = "Privileged endpoints for managing active and historical customer memberships")
public class AdminMembershipController {

    private final UserMembershipService userMembershipService;

    @PostMapping
    @Operation(summary = "Create user membership", description = "Admin-only endpoint to subscribe a user manually.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Membership created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User, plan, or tier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already has active membership or plan/tier is inactive")
    })
    public ResponseEntity<ApiResponse<UserMembershipResponse>> createMembership(@Valid @RequestBody CreateUserMembershipRequest request) {
        log.info("REST request to manually create membership: {}", request);
        UserMembershipResponse response = userMembershipService.createMembership(request, null);
        ApiResponse<UserMembershipResponse> apiResponse = ApiResponse.success(
                response,
                "Membership created successfully",
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all customer memberships (paginated)", description = "Admin-only endpoint to list all user loyalty membership records.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Memberships retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role")
    })
    public ResponseEntity<ApiResponse<PageResponse<UserMembershipResponse>>> getAllMemberships(@PageableDefault Pageable pageable) {
        log.info("REST request to list all memberships: {}", pageable);
        PageResponse<UserMembershipResponse> response = userMembershipService.getAllMemberships(pageable);
        ApiResponse<PageResponse<UserMembershipResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get membership by ID", description = "Admin-only endpoint to retrieve specific membership by UUID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Membership retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership not found")
    })
    public ResponseEntity<ApiResponse<UserMembershipResponse>> getMembershipById(@PathVariable String id) {
        log.info("REST request to retrieve membership: {}", id);
        UserMembershipResponse response = userMembershipService.getMembershipById(id);
        ApiResponse<UserMembershipResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}/upgrade")
    @Operation(summary = "Upgrade user membership", description = "Admin-only endpoint to upgrade a user's tier.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Membership upgraded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership or tier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Target tier priority is lower or equal, or target tier is inactive")
    })
    public ResponseEntity<ApiResponse<UserMembershipResponse>> upgradeMembership(
            @PathVariable String id,
            @Valid @RequestBody UpgradeMembershipRequest request) {
        log.info("REST request to upgrade membership: {}, target tier: {}", id, request.getMembershipTierId());
        UserMembershipResponse response = userMembershipService.upgradeMembership(id, request);
        ApiResponse<UserMembershipResponse> apiResponse = ApiResponse.success(
                response,
                "Membership upgraded successfully",
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel user membership", description = "Admin-only endpoint to cancel an active membership.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Membership cancelled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Membership is not ACTIVE")
    })
    public ResponseEntity<ApiResponse<UserMembershipResponse>> cancelMembership(
            @PathVariable String id,
            @Valid @RequestBody CancelMembershipRequest request) {
        log.info("REST request to cancel membership: {}, reason: {}", id, request.getReason());
        UserMembershipResponse response = userMembershipService.cancelMembership(id, request);
        ApiResponse<UserMembershipResponse> apiResponse = ApiResponse.success(
                response,
                "Membership cancelled successfully",
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }
}
