package com.devinder.loyalty.dto.response;

import com.devinder.loyalty.enums.CurrencyType;
import com.devinder.loyalty.enums.DurationUnit;
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
@Schema(description = "Membership plan details response payload")
public class MembershipPlanResponse {

    @Schema(description = "UUID of the membership plan", example = "3c4fa2d1-e63b-4890-bc4f-4d9298715c0e")
    private String id;

    @Schema(description = "Name of the membership plan", example = "Yearly Premium")
    private String name;

    @Schema(description = "Duration value", example = "1")
    private Integer duration;

    @Schema(description = "Duration unit (DAY, MONTH, YEAR)", example = "YEAR")
    private DurationUnit durationUnit;

    @Schema(description = "Base price stored in paise/cents", example = "9900")
    private Long basePrice;

    @Schema(description = "Currency type (USD, INR, EUR)", example = "INR")
    private CurrencyType currency;

    @Schema(description = "Active status flag of the plan", example = "true")
    private Boolean isActive;

    @Schema(description = "Timestamp when the plan was created", example = "2026-05-21T18:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the plan was last updated", example = "2026-05-21T18:30:00Z")
    private Instant updatedAt;
}
