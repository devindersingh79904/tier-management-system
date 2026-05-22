package com.devinder.loyalty.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for initiating a refund")
public class RefundPaymentRequest {

    @Size(max = 255, message = "Refund reason must not exceed 255 characters")
    @Schema(description = "Reason for initiating refund", example = "Customer requested cancellation")
    private String reason;
}
