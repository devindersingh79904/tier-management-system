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
@Schema(description = "Request body for cancelling a user membership")
public class CancelMembershipRequest {

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    @Schema(description = "Reason for cancellation", example = "No longer needed")
    private String reason;
}
