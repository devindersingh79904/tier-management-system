package com.devinder.loyalty.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a user membership")
public class CreateUserMembershipRequest {

    @NotBlank(message = "Membership plan ID is required")
    @Schema(description = "UUID of the membership plan", example = "3c4fa2d1-e63b-4890-bc4f-4d9298715c0e", requiredMode = Schema.RequiredMode.REQUIRED)
    private String membershipPlanId;

    @NotBlank(message = "Membership tier ID is required")
    @Schema(description = "UUID of the membership tier", example = "a2c16e78-bc5a-4712-8822-79015c92c55b", requiredMode = Schema.RequiredMode.REQUIRED)
    private String membershipTierId;

    @NotNull(message = "Auto renew flag is required")
    @Schema(description = "Flag indicating whether subscription should auto-renew", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean autoRenew;

    @Schema(description = "UUID of the user (admin-only endpoint target user)", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private String userId;
}
