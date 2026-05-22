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
@Schema(description = "Membership benefit details response payload")
public class MembershipBenefitResponse {

    @Schema(description = "UUID of the membership benefit", example = "a2c16e78-bc5a-4712-8822-79015c92c55b")
    private String id;

    @Schema(description = "Name of the benefit", example = "Free Shipping")
    private String name;

    @Schema(description = "Description of the benefit", example = "Provides free shipping on all orders without minimum spend")
    private String description;

    @Schema(description = "Flag indicating whether this benefit is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Timestamp when the benefit was created", example = "2026-05-21T18:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the benefit was last updated", example = "2026-05-21T18:30:00Z")
    private Instant updatedAt;
}
