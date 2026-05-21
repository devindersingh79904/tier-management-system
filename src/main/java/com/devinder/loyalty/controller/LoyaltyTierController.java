package com.devinder.loyalty.controller;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.request.LoyaltyTierRequest;
import com.devinder.loyalty.dto.response.LoyaltyTierResponse;
import com.devinder.loyalty.service.LoyaltyTierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tiers")
@RequiredArgsConstructor
@Tag(name = "Loyalty Tiers", description = "Endpoints for managing Loyalty Tiers and Levels")
public class LoyaltyTierController {

    private final LoyaltyTierService service;

    @PostMapping
    @Operation(summary = "Create a new loyalty tier")
    public ResponseEntity<ApiResponse<LoyaltyTierResponse>> createTier(@Valid @RequestBody LoyaltyTierRequest request) {
        LoyaltyTierResponse responseData = service.createTier(request);
        ApiResponse<LoyaltyTierResponse> response = ApiResponse.success(responseData, MessageConstants.TIER_CREATED, HttpStatus.CREATED.value());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get loyalty tier by ID")
    public ResponseEntity<ApiResponse<LoyaltyTierResponse>> getTierById(@PathVariable Long id) {
        LoyaltyTierResponse responseData = service.getTierById(id);
        ApiResponse<LoyaltyTierResponse> response = ApiResponse.success(responseData, MessageConstants.SUCCESS);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing loyalty tier")
    public ResponseEntity<ApiResponse<LoyaltyTierResponse>> updateTier(
            @PathVariable Long id, 
            @Valid @RequestBody LoyaltyTierRequest request) {
        LoyaltyTierResponse responseData = service.updateTier(id, request);
        ApiResponse<LoyaltyTierResponse> response = ApiResponse.success(responseData, MessageConstants.TIER_UPDATED);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a loyalty tier")
    public ResponseEntity<ApiResponse<Void>> deleteTier(@PathVariable Long id) {
        service.deleteTier(id);
        ApiResponse<Void> response = ApiResponse.success(null, MessageConstants.TIER_DELETED);
        return ResponseEntity.ok(response);
    }
}
