package com.devinder.loyalty.dto.request;

import com.devinder.loyalty.enums.CurrencyType;
import com.devinder.loyalty.enums.DurationUnit;
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
@Schema(description = "Request body for creating a membership plan")
public class CreateMembershipPlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name must not exceed 100 characters")
    @Schema(description = "Name of the membership plan", example = "Yearly Premium", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1")
    @Schema(description = "Duration value", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer duration;

    @NotNull(message = "Duration unit is required")
    @Schema(description = "Duration unit (DAY, MONTH, YEAR)", example = "YEAR", requiredMode = Schema.RequiredMode.REQUIRED)
    private DurationUnit durationUnit;

    @NotNull(message = "Base price is required")
    @Min(value = 0, message = "Base price must be 0 or positive")
    @Schema(description = "Base price stored in paise/cents", example = "9900", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long basePrice;

    @NotNull(message = "Currency is required")
    @Schema(description = "Currency type (USD, INR, EUR)", example = "INR", requiredMode = Schema.RequiredMode.REQUIRED)
    private CurrencyType currency;

    @Builder.Default
    @Schema(description = "Flag indicating whether this plan is active", example = "true")
    private Boolean isActive = true;
}
