package com.devinder.loyalty.controller.user;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.request.CreatePaymentIntentRequest;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.dto.response.PaymentIntentResponse;
import com.devinder.loyalty.service.PaymentIntentService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/me/payments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "User Payment APIs", description = "Customer self-service endpoints to create and view payments history")
public class UserPaymentIntentController {

    private final PaymentIntentService paymentIntentService;

    @PostMapping
    @Operation(summary = "Create payment intent", description = "Customer self-service endpoint to capture a payment or renewal transaction.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Payment processed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Membership not active, or invalid transaction parameters")
    })
    public ResponseEntity<ApiResponse<PaymentIntentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentIntentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("REST request from user {} to process payment: {}", username, request);
        PaymentIntentResponse response = paymentIntentService.createPayment(request, username);
        ApiResponse<PaymentIntentResponse> apiResponse = ApiResponse.success(
                response,
                "Payment processed successfully",
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get my payments (paginated)", description = "Retrieve all payment transaction history matching the authenticated customer context.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments history retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token")
    })
    public ResponseEntity<ApiResponse<PageResponse<PaymentIntentResponse>>> getMyPayments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault Pageable pageable) {
        String username = userDetails.getUsername();
        log.info("REST request to list my payments for user {}: {}", username, pageable);
        PageResponse<PaymentIntentResponse> response = paymentIntentService.getMyPayments(username, pageable);
        ApiResponse<PageResponse<PaymentIntentResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get my payment by ID", description = "Retrieve specific payment intent transaction details by ID for the logged-in customer context.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment record not found")
    })
    public ResponseEntity<ApiResponse<PaymentIntentResponse>> getMyPaymentById(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("REST request to get payment {} for user {}", id, username);
        PaymentIntentResponse response = paymentIntentService.getMyPaymentById(id, username);
        ApiResponse<PaymentIntentResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS
        );
        return ResponseEntity.ok(apiResponse);
    }
}
