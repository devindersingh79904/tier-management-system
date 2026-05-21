package com.devinder.loyalty.controller.user;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.response.MembershipTierResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.service.MembershipTierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/tiers")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "User APIs", description = "Customer self-service endpoints")
public class UserMembershipTierController {

    private final MembershipTierService membershipTierService;

    @GetMapping("/{id}")
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
}
