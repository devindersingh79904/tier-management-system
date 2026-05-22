package com.devinder.loyalty.dto.request;

import com.devinder.loyalty.enums.PaymentMethod;
import com.devinder.loyalty.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a payment intent")
public class CreatePaymentIntentRequest {

    @NotBlank(message = "User membership ID is required")
    @Schema(description = "UUID of the user membership", example = "3c4fa2d1-e63b-4890-bc4f-4d9298715c0e", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userMembershipId;

    @NotNull(message = "Transaction type is required")
    @Schema(description = "Transaction type of the payment", example = "PAYMENT", requiredMode = Schema.RequiredMode.REQUIRED)
    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be greater than or equal to 0")
    @Schema(description = "Amount in paise/cents", example = "5000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long amount;

    @NotNull(message = "Payment method is required")
    @Schema(description = "Payment method used", example = "CARD", requiredMode = Schema.RequiredMode.REQUIRED)
    private PaymentMethod paymentMethod;

    @NotBlank(message = "Transaction ID is required")
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    @Schema(description = "Transaction ID from gateway", example = "ch_3Mv2Z1LkdIwHu7ix1gqW8n21", requiredMode = Schema.RequiredMode.REQUIRED)
    private String transactionId;

    @NotBlank(message = "Payment provider is required")
    @Size(max = 50, message = "Payment provider must not exceed 50 characters")
    @Schema(description = "Payment provider name", example = "STRIPE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentProvider;

    @NotBlank(message = "Idempotency key is required")
    @Size(max = 100, message = "Idempotency key must not exceed 100 characters")
    @Schema(description = "Unique idempotency key to prevent double charging", example = "ide_key_889218", requiredMode = Schema.RequiredMode.REQUIRED)
    private String idempotencyKey;
}
