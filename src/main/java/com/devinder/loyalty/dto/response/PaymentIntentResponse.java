package com.devinder.loyalty.dto.response;

import com.devinder.loyalty.enums.PaymentMethod;
import com.devinder.loyalty.enums.PaymentStatus;
import com.devinder.loyalty.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment intent response payload")
public class PaymentIntentResponse {

    @Schema(description = "UUID of the payment record", example = "92bf88e1-561b-4f81-a67b-12d8376483fb")
    private String id;

    @Schema(description = "UUID of the associated user membership", example = "3c4fa2d1-e63b-4890-bc4f-4d9298715c0e")
    private String userMembershipId;

    @Schema(description = "Transaction type (e.g. PAYMENT, RENEWAL, REFUND, UPGRADE_CHARGE)", example = "PAYMENT")
    private TransactionType transactionType;

    @Schema(description = "Amount in paise/cents", example = "5000")
    private Long amount;

    @Schema(description = "Payment status (e.g. PENDING, SUCCESS, FAILED, REFUNDED)", example = "SUCCESS")
    private PaymentStatus paymentStatus;

    @Schema(description = "Unique transaction ID from provider", example = "ch_3Mv2Z1LkdIwHu7ix1gqW8n21")
    private String transactionId;

    @Schema(description = "Payment provider name", example = "STRIPE")
    private String paymentProvider;

    @Schema(description = "Unique idempotency key used for request", example = "ide_key_889218")
    private String idempotencyKey;

    @Schema(description = "Payment method used", example = "CARD")
    private PaymentMethod paymentMethod;

    @Schema(description = "Timestamp when the payment intent was created", example = "2026-05-21T18:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the payment intent was last updated", example = "2026-05-21T18:30:00Z")
    private Instant updatedAt;
}
