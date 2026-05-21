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
@Schema(description = "Membership tier details response payload")
public class MembershipTierResponse {

    @Schema(description = "UUID of the membership tier", example = "a2c16e78-bc5a-4712-8822-79015c92c55b")
    private String id;

    @Schema(description = "Name of the membership tier", example = "GOLD")
    private String name;

    @Schema(description = "Priority number of the tier", example = "2")
    private Integer priority;

    @Schema(description = "Description of benefits in this tier", example = "Premium benefits like early delivery and priority support.")
    private String description;

    @Schema(description = "Active status flag of the tier", example = "true")
    private Boolean isActive;

    @Schema(description = "Timestamp when the tier was created", example = "2026-05-21T18:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the tier was last updated", example = "2026-05-21T18:30:00Z")
    private Instant updatedAt;
}
