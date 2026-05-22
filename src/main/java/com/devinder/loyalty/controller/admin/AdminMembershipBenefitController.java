package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.request.CreateMembershipBenefitRequest;
import com.devinder.loyalty.dto.request.UpdateMembershipBenefitRequest;
import com.devinder.loyalty.dto.response.MembershipBenefitResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.service.MembershipBenefitService;
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
@RequestMapping("/api/v1/admin/benefits")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Tag(name = "Admin Benefit APIs", description = "Privileged endpoints for managing membership benefit catalog")
public class AdminMembershipBenefitController {

    private final MembershipBenefitService membershipBenefitService;

    @PostMapping
    @Operation(summary = "Create a new membership benefit", description = "Admin-only endpoint to create a loyalty membership benefit.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Benefit created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request input details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict due to duplicate name")
    })
    public ResponseEntity<ApiResponse<MembershipBenefitResponse>> createBenefit(@Valid @RequestBody CreateMembershipBenefitRequest request) {
        log.info("REST request to create MembershipBenefit: {}", request.getName());
        MembershipBenefitResponse response = membershipBenefitService.createBenefit(request);
        ApiResponse<MembershipBenefitResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.BENEFIT_CREATED,
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing membership benefit", description = "Admin-only endpoint to update membership benefit details.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Benefit updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request input details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Benefit not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict due to duplicate name or active configurations preventing deactivation")
    })
    public ResponseEntity<ApiResponse<MembershipBenefitResponse>> updateBenefit(
            @PathVariable String id,
            @Valid @RequestBody UpdateMembershipBenefitRequest request) {
        log.info("REST request to update MembershipBenefit id: {}, name: {}", id, request.getName());
        MembershipBenefitResponse response = membershipBenefitService.updateBenefit(id, request);
        ApiResponse<MembershipBenefitResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.BENEFIT_UPDATED,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a membership benefit by ID", description = "Admin-only endpoint to retrieve benefit details by UUID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Benefit retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Benefit not found")
    })
    public ResponseEntity<ApiResponse<MembershipBenefitResponse>> getBenefitById(@PathVariable String id) {
        log.info("REST request to get MembershipBenefit by id: {}", id);
        MembershipBenefitResponse response = membershipBenefitService.getBenefitById(id);
        ApiResponse<MembershipBenefitResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    @Operation(summary = "Get all membership benefits (paginated)", description = "Admin-only endpoint to list all available membership benefits.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Benefits retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role")
    })
    public ResponseEntity<ApiResponse<PageResponse<MembershipBenefitResponse>>> getAllBenefits(@PageableDefault Pageable pageable) {
        log.info("REST request to get all MembershipBenefits: {}", pageable);
        PageResponse<MembershipBenefitResponse> response = membershipBenefitService.getAllBenefits(pageable);
        ApiResponse<PageResponse<MembershipBenefitResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate/Soft delete a membership benefit", description = "Admin-only endpoint to soft delete/deactivate a benefit by UUID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Benefit deactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Benefit not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict due to active benefit configurations using the benefit")
    })
    public ResponseEntity<ApiResponse<Void>> deleteBenefit(@PathVariable String id) {
        log.info("REST request to soft delete/deactivate MembershipBenefit by id: {}", id);
        membershipBenefitService.deleteBenefit(id);
        ApiResponse<Void> apiResponse = ApiResponse.success(
                null,
                MessageConstants.BENEFIT_DELETED,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }
}
