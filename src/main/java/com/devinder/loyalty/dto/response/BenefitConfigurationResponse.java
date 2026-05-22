package com.devinder.loyalty.dto.response;

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
@Schema(description = "Benefit configuration details response payload")
public class BenefitConfigurationResponse {

    @Schema(description = "UUID of the benefit configuration", example = "92bf88e1-561b-4f81-a67b-12d8376483fb")
    private String id;

    @Schema(description = "UUID of the membership benefit", example = "a2c16e78-bc5a-4712-8822-79015c92c55b")
    private String membershipBenefitId;

    @Schema(description = "Name of the membership benefit", example = "Free Shipping")
    private String membershipBenefitName;

    @Schema(description = "UUID of the membership plan (optional)", example = "3c4fa2d1-e63b-4890-bc4f-4d9298715c0e")
    private String membershipPlanId;

    @Schema(description = "Name of the membership plan (optional)", example = "Yearly Premium")
    private String membershipPlanName;

    @Schema(description = "UUID of the membership tier (optional)", example = "8e9b62a1-0f7d-41a2-b258-39c288921e90")
    private String membershipTierId;

    @Schema(description = "Name of the membership tier (optional)", example = "GOLD")
    private String membershipTierName;

    @Schema(description = "Valid JSON representation of the configuration settings", example = "{\"discountPercent\": 10, \"maxCap\": 500}")
    private String configurationJson;

    @Schema(description = "Flag indicating whether this configuration is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Timestamp when the configuration was created", example = "2026-05-21T18:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the configuration was last updated", example = "2026-05-21T18:30:00Z")
    private Instant updatedAt;
}
