package com.devinder.loyalty.controller;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.request.CreateMembershipTierRequest;
import com.devinder.loyalty.dto.request.UpdateMembershipTierRequest;
import com.devinder.loyalty.dto.response.MembershipTierResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.service.MembershipTierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/tiers")
@RequiredArgsConstructor
@Tag(name = "Membership Tier Management", description = "Endpoints for managing customer loyalty membership tiers")
public class MembershipTierController {

    private final MembershipTierService membershipTierService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new membership tier", description = "Admin-only endpoint to create a loyalty membership tier with unique name and priority.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tier created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request input details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict due to duplicate name or priority")
    })
    public ResponseEntity<ApiResponse<MembershipTierResponse>> createTier(@Valid @RequestBody CreateMembershipTierRequest request) {
        MembershipTierResponse response = membershipTierService.createTier(request);
        ApiResponse<MembershipTierResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.TIER_CREATED,
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing membership tier", description = "Admin-only endpoint to update an existing membership tier. Validates uniqueness of name and priority and supports optimistic locking.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tier updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request input details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership tier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict due to duplicate name, priority, concurrent update, or active user memberships")
    })
    public ResponseEntity<ApiResponse<MembershipTierResponse>> updateTier(
            @PathVariable String id,
            @Valid @RequestBody UpdateMembershipTierRequest request) {
        MembershipTierResponse response = membershipTierService.updateTier(id, request);
        ApiResponse<MembershipTierResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.TIER_UPDATED,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get membership tier by ID", description = "Retrieve complete details of a specific membership tier by its unique ID. Accessible by any authenticated user.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tier retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership tier not found")
    })
    public ResponseEntity<ApiResponse<MembershipTierResponse>> getTierById(@PathVariable String id) {
        MembershipTierResponse response = membershipTierService.getTierById(id);
        ApiResponse<MembershipTierResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all membership tiers paginated", description = "Retrieve a pageable list of membership tiers. Accessible by any authenticated user.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tiers retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token")
    })
    public ResponseEntity<ApiResponse<PageResponse<MembershipTierResponse>>> getAllTiers(@ParameterObject Pageable pageable) {
        PageResponse<MembershipTierResponse> response = membershipTierService.getAllTiers(pageable);
        ApiResponse<PageResponse<MembershipTierResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete a membership tier", description = "Admin-only endpoint to soft delete a membership tier. Disables the active status of the tier if no active customer memberships are currently linked.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tier soft-deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership tier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict due to active user memberships linked to this tier")
    })
    public ResponseEntity<ApiResponse<Void>> deleteTier(@PathVariable String id) {
        membershipTierService.deleteTier(id);
        ApiResponse<Void> apiResponse = ApiResponse.success(
                null,
                MessageConstants.TIER_DELETED,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }
}
