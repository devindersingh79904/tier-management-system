package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.request.CreateMembershipPlanRequest;
import com.devinder.loyalty.dto.request.UpdateMembershipPlanRequest;
import com.devinder.loyalty.dto.response.MembershipPlanResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.service.MembershipPlanService;
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
@RequestMapping("/api/v1/admin/plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Tag(name = "Admin Plan APIs", description = "Privileged endpoints for managing membership billing plans")
public class AdminMembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    @PostMapping
    @Operation(summary = "Create a new membership plan", description = "Admin-only endpoint to create a loyalty membership plan.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Plan created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request input details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict due to duplicate name")
    })
    public ResponseEntity<ApiResponse<MembershipPlanResponse>> createPlan(@Valid @RequestBody CreateMembershipPlanRequest request) {
        log.info("REST request to create MembershipPlan: {}", request.getName());
        MembershipPlanResponse response = membershipPlanService.createPlan(request);
        ApiResponse<MembershipPlanResponse> apiResponse = ApiResponse.success(
                response,
                "Membership plan created successfully",
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing membership plan", description = "Admin-only endpoint to update a membership plan details.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plan updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request input details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Plan not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict due to duplicate name or active subscriptions preventing deactivation")
    })
    public ResponseEntity<ApiResponse<MembershipPlanResponse>> updatePlan(
            @PathVariable String id,
            @Valid @RequestBody UpdateMembershipPlanRequest request) {
        log.info("REST request to update MembershipPlan: {}, id: {}", request.getName(), id);
        MembershipPlanResponse response = membershipPlanService.updatePlan(id, request);
        ApiResponse<MembershipPlanResponse> apiResponse = ApiResponse.success(
                response,
                "Membership plan updated successfully",
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a membership plan by ID", description = "Admin-only endpoint to retrieve plan details by UUID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plan retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Plan not found")
    })
    public ResponseEntity<ApiResponse<MembershipPlanResponse>> getPlanById(@PathVariable String id) {
        log.info("REST request to get MembershipPlan by id: {}", id);
        MembershipPlanResponse response = membershipPlanService.getPlanById(id);
        ApiResponse<MembershipPlanResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    @Operation(summary = "Get all membership plans (paginated)", description = "Admin-only endpoint to list all available membership plans.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plans retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role")
    })
    public ResponseEntity<ApiResponse<PageResponse<MembershipPlanResponse>>> getAllPlans(@PageableDefault Pageable pageable) {
        log.info("REST request to get all MembershipPlans: {}", pageable);
        PageResponse<MembershipPlanResponse> response = membershipPlanService.getAllPlans(pageable);
        ApiResponse<PageResponse<MembershipPlanResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate/Soft delete a membership plan", description = "Admin-only endpoint to deactivate a plan by UUID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plan deactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Plan not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict due to active customer subscriptions using the plan")
    })
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable String id) {
        log.info("REST request to soft delete/deactivate MembershipPlan by id: {}", id);
        membershipPlanService.deletePlan(id);
        ApiResponse<Void> apiResponse = ApiResponse.success(
                null,
                "Membership plan deactivated successfully",
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }
}
