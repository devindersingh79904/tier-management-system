package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.request.CreateBenefitConfigurationRequest;
import com.devinder.loyalty.dto.request.UpdateBenefitConfigurationRequest;
import com.devinder.loyalty.dto.response.BenefitConfigurationResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.service.BenefitConfigurationService;
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
@RequestMapping("/api/v1/admin/benefit-configurations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Tag(name = "Admin Benefit Configuration APIs", description = "Privileged endpoints for linking and configuring benefits to plans/tiers")
public class AdminBenefitConfigurationController {

    private final BenefitConfigurationService benefitConfigurationService;

    @PostMapping
    @Operation(summary = "Create a new benefit configuration", description = "Admin-only endpoint to map benefit perks to specific membership plans or tiers with custom JSON parameters.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Configuration created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request input details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict due to pre-existing active configuration")
    })
    public ResponseEntity<ApiResponse<BenefitConfigurationResponse>> createConfiguration(@Valid @RequestBody CreateBenefitConfigurationRequest request) {
        log.info("REST request to create BenefitConfiguration: {}", request);
        BenefitConfigurationResponse response = benefitConfigurationService.createConfiguration(request);
        ApiResponse<BenefitConfigurationResponse> apiResponse = ApiResponse.success(
                response,
                "Benefit configuration created successfully",
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing benefit configuration", description = "Admin-only endpoint to update the JSON parameter configurations or status of a mapping.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuration updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request input details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public ResponseEntity<ApiResponse<BenefitConfigurationResponse>> updateConfiguration(
            @PathVariable String id,
            @Valid @RequestBody UpdateBenefitConfigurationRequest request) {
        log.info("REST request to update BenefitConfiguration ID: {}", id);
        BenefitConfigurationResponse response = benefitConfigurationService.updateConfiguration(id, request);
        ApiResponse<BenefitConfigurationResponse> apiResponse = ApiResponse.success(
                response,
                "Benefit configuration updated successfully",
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a benefit configuration by ID", description = "Admin-only endpoint to retrieve configuration details by UUID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuration retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public ResponseEntity<ApiResponse<BenefitConfigurationResponse>> getConfigurationById(@PathVariable String id) {
        log.info("REST request to get BenefitConfiguration by ID: {}", id);
        BenefitConfigurationResponse response = benefitConfigurationService.getConfigurationById(id);
        ApiResponse<BenefitConfigurationResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    @Operation(summary = "Get all benefit configurations (paginated)", description = "Admin-only endpoint to list all benefit mappings.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configurations retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role")
    })
    public ResponseEntity<ApiResponse<PageResponse<BenefitConfigurationResponse>>> getAllConfigurations(@PageableDefault Pageable pageable) {
        log.info("REST request to get all BenefitConfigurations: {}", pageable);
        PageResponse<BenefitConfigurationResponse> response = benefitConfigurationService.getAllConfigurations(pageable);
        ApiResponse<PageResponse<BenefitConfigurationResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate/Soft delete a benefit configuration", description = "Admin-only endpoint to soft delete/deactivate a configuration mapping by UUID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuration deactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteConfiguration(@PathVariable String id) {
        log.info("REST request to soft delete/deactivate BenefitConfiguration by ID: {}", id);
        benefitConfigurationService.deleteConfiguration(id);
        ApiResponse<Void> apiResponse = ApiResponse.success(
                null,
                "Benefit configuration deactivated successfully",
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }
}
