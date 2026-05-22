package com.devinder.loyalty.controller.user;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.response.MembershipPlanResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.service.MembershipPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "User Plan APIs", description = "User endpoints for viewing active billing plans")
public class UserMembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    @GetMapping
    @Operation(summary = "Get active membership plans", description = "List all active membership plans paginated.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plans retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token")
    })
    public ResponseEntity<ApiResponse<PageResponse<MembershipPlanResponse>>> getActivePlans(@PageableDefault Pageable pageable) {
        log.info("REST request to get active MembershipPlans: {}", pageable);
        PageResponse<MembershipPlanResponse> response = membershipPlanService.getActivePlans(pageable);
        ApiResponse<PageResponse<MembershipPlanResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get active membership plan by ID", description = "Retrieve details of an active plan by UUID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plan retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Plan not found or inactive")
    })
    public ResponseEntity<ApiResponse<MembershipPlanResponse>> getActivePlanById(@PathVariable String id) {
        log.info("REST request to get active MembershipPlan by id: {}", id);
        MembershipPlanResponse response = membershipPlanService.getPlanById(id);
        // We can throw 404 if the plan is inactive, as a business safety check
        if (!Boolean.TRUE.equals(response.getIsActive())) {
            log.warn("Attempt to retrieve inactive plan {} by user", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ApiResponse<MembershipPlanResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }
}
