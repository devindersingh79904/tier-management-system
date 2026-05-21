package com.devinder.loyalty.dto.request;

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
@Schema(description = "Request body for creating a membership tier")
public class CreateMembershipTierRequest {

    @NotBlank(message = "Tier name is required")
    @Size(max = 50, message = "Tier name must not exceed 50 characters")
    @Schema(description = "Unique name of the membership tier", example = "GOLD", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Tier priority is required")
    @Min(value = 0, message = "Tier priority must be 0 or positive")
    @Schema(description = "Unique priority sequence number where lower number is higher priority", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer priority;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Schema(description = "Optional descriptive overview of the benefits offered in this tier", example = "Premium gold status benefits including priority customer support and cashback.")
    private String description;

    @Builder.Default
    @Schema(description = "Flag indicating whether this tier is currently active", example = "true")
    private Boolean isActive = true;
}
