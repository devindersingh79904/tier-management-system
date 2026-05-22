package com.devinder.loyalty.controller.admin;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.dto.ApiResponse;
import com.devinder.loyalty.dto.request.RefundPaymentRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Tag(name = "Admin Payment APIs", description = "Privileged endpoints for monitoring transactions and processing refunds")
public class AdminPaymentIntentController {

    private final PaymentIntentService paymentIntentService;

    @GetMapping
    @Operation(summary = "Get all payments (paginated)", description = "Admin-only endpoint to retrieve all transaction records.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden access role")
    })
    public ResponseEntity<ApiResponse<PageResponse<PaymentIntentResponse>>> getAllPayments(@PageableDefault Pageable pageable) {
        log.info("REST request to list all payments: {}", pageable);
        PageResponse<PaymentIntentResponse> response = paymentIntentService.getAllPayments(pageable);
        ApiResponse<PageResponse<PaymentIntentResponse>> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Admin-only endpoint to retrieve details for a specific payment ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<ApiResponse<PaymentIntentResponse>> getPaymentById(@PathVariable String id) {
        log.info("REST request to retrieve payment: {}", id);
        PaymentIntentResponse response = paymentIntentService.getPaymentById(id);
        ApiResponse<PaymentIntentResponse> apiResponse = ApiResponse.success(
                response,
                MessageConstants.SUCCESS
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}/refund")
    @Operation(summary = "Initiate refund for payment", description = "Admin-only endpoint to trigger a manual refund on a successful transaction.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment refunded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Payment cannot be refunded (not SUCCESS, or already REFUNDED)")
    })
    public ResponseEntity<ApiResponse<PaymentIntentResponse>> refundPayment(
            @PathVariable String id,
            @Valid @RequestBody RefundPaymentRequest request) {
        log.info("REST request to refund payment: {}, reason: {}", id, request.getReason());
        PaymentIntentResponse response = paymentIntentService.refundPayment(id, request);
        ApiResponse<PaymentIntentResponse> apiResponse = ApiResponse.success(
                response,
                "Payment refunded successfully"
        );
        return ResponseEntity.ok(apiResponse);
    }
}
