package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.request.CreateMembershipPlanRequest;
import com.devinder.loyalty.dto.response.MembershipPlanResponse;
import com.devinder.loyalty.service.MembershipPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Tag(name = "Admin Plan APIs", description = "Privileged endpoints for managing membership billing plans")
public class AdminMembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    @PostMapping
    @Operation(summary = "Create a new membership plan", description = "Admin-only endpoint to create a loyalty membership plan with unique name.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Plan created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request input details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict due to duplicate name")
    })
    public ResponseEntity<ApiResponse<MembershipPlanResponse>> createPlan(@Valid @RequestBody CreateMembershipPlanRequest request) {
        MembershipPlanResponse response = membershipPlanService.createPlan(request);
        ApiResponse<MembershipPlanResponse> apiResponse = ApiResponse.success(
                response,
                "Membership plan created successfully",
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all membership plans", description = "Admin-only endpoint to list all available membership plans.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plans retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role")
    })
    public ResponseEntity<ApiResponse<List<MembershipPlanResponse>>> getAllPlans() {
        List<MembershipPlanResponse> response = membershipPlanService.getAllPlans();
        ApiResponse<List<MembershipPlanResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }
}
