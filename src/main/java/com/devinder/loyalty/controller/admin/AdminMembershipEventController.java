package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.response.MembershipEventResponse;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.service.MembershipEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/membership-events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Tag(name = "Admin Membership Event APIs", description = "Privileged endpoints for viewing membership transition and lifecycle history")
public class AdminMembershipEventController {

    private final MembershipEventService membershipEventService;

    @GetMapping
    @Operation(summary = "Get all membership events (paginated)", description = "Admin-only endpoint to list all membership state change events.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role")
    })
    public ResponseEntity<ApiResponse<PageResponse<MembershipEventResponse>>> getAllEvents(@PageableDefault Pageable pageable) {
        log.info("REST request to list all membership events: {}", pageable);
        PageResponse<MembershipEventResponse> response = membershipEventService.getAllEvents(pageable);
        ApiResponse<PageResponse<MembershipEventResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get membership event by ID", description = "Admin-only endpoint to retrieve specific event details by ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Event retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<ApiResponse<MembershipEventResponse>> getEventById(@PathVariable String id) {
        log.info("REST request to retrieve membership event: {}", id);
        MembershipEventResponse response = membershipEventService.getEventById(id);
        ApiResponse<MembershipEventResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/membership/{membershipId}")
    @Operation(summary = "Get events by membership ID (paginated)", description = "Admin-only endpoint to retrieve state history for a specific membership ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership not found")
    })
    public ResponseEntity<ApiResponse<PageResponse<MembershipEventResponse>>> getEventsByMembershipId(
            @PathVariable String membershipId,
            @PageableDefault Pageable pageable) {
        log.info("REST request to retrieve membership events for membership ID: {}, pageable: {}", membershipId, pageable);
        PageResponse<MembershipEventResponse> response = membershipEventService.getEventsByMembershipId(membershipId, pageable);
        ApiResponse<PageResponse<MembershipEventResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS
        );
        return ResponseEntity.ok(apiResponse);
    }
}
