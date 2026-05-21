package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.response.UserMembershipResponse;
import com.devinder.loyalty.service.UserMembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/memberships")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Tag(name = "Admin Membership APIs", description = "Privileged endpoints for managing active and historical customer memberships")
public class AdminMembershipController {

    private final UserMembershipService userMembershipService;

    @GetMapping
    @Operation(summary = "Get all customer memberships", description = "Admin-only endpoint to list all user loyalty membership records.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Memberships retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role")
    })
    public ResponseEntity<ApiResponse<List<UserMembershipResponse>>> getAllMemberships() {
        List<UserMembershipResponse> response = userMembershipService.getAllMemberships();
        ApiResponse<List<UserMembershipResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(apiResponse);
    }
}
