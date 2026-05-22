package com.devinder.loyalty.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for upgrading a user membership")
public class UpgradeMembershipRequest {

    @NotBlank(message = "Membership tier ID is required")
    @Schema(description = "UUID of the target membership tier", example = "a2c16e78-bc5a-4712-8822-79015c92c55b", requiredMode = Schema.RequiredMode.REQUIRED)
    private String membershipTierId;
}
