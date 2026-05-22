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
@Schema(description = "Tier criteria details response payload")
public class TierCriteriaResponse {

    @Schema(description = "UUID of the tier criteria record", example = "92bf88e1-561b-4f81-a67b-12d8376483fb")
    private String id;

    @Schema(description = "UUID of the associated membership tier", example = "a2c16e78-bc5a-4712-8822-79015c92c55b")
    private String membershipTierId;

    @Schema(description = "Name of the associated membership tier", example = "GOLD")
    private String membershipTierName;

    @Schema(description = "Valid JSON representation of the evaluation criteria rules", example = "{\"operator\": \"AND\", \"rules\": [{\"field\": \"totalSpent\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 10000}]}")
    private String criteriaJson;

    @Schema(description = "Flag indicating whether this criteria is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Timestamp when the criteria was created", example = "2026-05-21T18:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the criteria was last updated", example = "2026-05-21T18:30:00Z")
    private Instant updatedAt;
}
