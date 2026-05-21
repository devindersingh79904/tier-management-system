package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.request.CreateMembershipTierRequest;
import com.devinder.loyalty.dto.request.UpdateMembershipTierRequest;
import com.devinder.loyalty.dto.response.MembershipTierResponse;
import com.devinder.loyalty.service.MembershipTierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/tiers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Tag(name = "Admin Tier APIs", description = "Privileged endpoints for managing membership tiers")
public class AdminMembershipTierController {

    private final MembershipTierService membershipTierService;

    @PostMapping
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

    @DeleteMapping("/{id}")
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
