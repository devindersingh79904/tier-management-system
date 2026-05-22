package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.request.CreateTierCriteriaRequest;
import com.devinder.loyalty.dto.request.UpdateTierCriteriaRequest;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.dto.response.TierCriteriaResponse;
import com.devinder.loyalty.service.TierCriteriaService;
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
@RequestMapping("/api/v1/admin/tier-criteria")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Tag(name = "Admin Tier Criteria APIs", description = "Privileged endpoints for managing eligibility evaluation criteria")
public class AdminTierCriteriaController {

    private final TierCriteriaService tierCriteriaService;

    @PostMapping
    @Operation(summary = "Create a new tier evaluation criteria", description = "Admin-only endpoint to define rules determining eligibility for a loyalty tier using custom JSON criteria.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Criteria created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request input details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict due to pre-existing active criteria")
    })
    public ResponseEntity<ApiResponse<TierCriteriaResponse>> createCriteria(@Valid @RequestBody CreateTierCriteriaRequest request) {
        log.info("REST request to create TierCriteria: {}", request);
        TierCriteriaResponse response = tierCriteriaService.createCriteria(request);
        ApiResponse<TierCriteriaResponse> apiResponse = ApiResponse.success(
                response,
                "Tier criteria created successfully",
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing tier evaluation criteria", description = "Admin-only endpoint to update rules or active status for a criteria mapping.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Criteria updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request input details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Criteria not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict due to another active criteria config for the same tier")
    })
    public ResponseEntity<ApiResponse<TierCriteriaResponse>> updateCriteria(
            @PathVariable String id,
            @Valid @RequestBody UpdateTierCriteriaRequest request) {
        log.info("REST request to update TierCriteria ID: {}", id);
        TierCriteriaResponse response = tierCriteriaService.updateCriteria(id, request);
        ApiResponse<TierCriteriaResponse> apiResponse = ApiResponse.success(
                response,
                "Tier criteria updated successfully",
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a tier criteria by ID", description = "Admin-only endpoint to retrieve criteria details by UUID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Criteria retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Criteria not found")
    })
    public ResponseEntity<ApiResponse<TierCriteriaResponse>> getCriteriaById(@PathVariable String id) {
        log.info("REST request to get TierCriteria by ID: {}", id);
        TierCriteriaResponse response = tierCriteriaService.getCriteriaById(id);
        ApiResponse<TierCriteriaResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    @Operation(summary = "Get all tier criteria records (paginated)", description = "Admin-only endpoint to list all criteria rules.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Criteria records retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role")
    })
    public ResponseEntity<ApiResponse<PageResponse<TierCriteriaResponse>>> getAllCriteria(@PageableDefault Pageable pageable) {
        log.info("REST request to get all TierCriteria records: {}", pageable);
        PageResponse<TierCriteriaResponse> response = tierCriteriaService.getAllCriteria(pageable);
        ApiResponse<PageResponse<TierCriteriaResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate/Soft delete a tier criteria", description = "Admin-only endpoint to soft delete/deactivate a criteria by UUID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Criteria deactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Criteria not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCriteria(@PathVariable String id) {
        log.info("REST request to soft delete/deactivate TierCriteria by ID: {}", id);
        tierCriteriaService.deleteCriteria(id);
        ApiResponse<Void> apiResponse = ApiResponse.success(
                null,
                "Tier criteria deactivated successfully",
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }
}
