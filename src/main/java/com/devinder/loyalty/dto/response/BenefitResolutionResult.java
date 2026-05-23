package com.devinder.loyalty.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resolved benefit details for a membership")
public class BenefitResolutionResult {

    @Schema(description = "Benefit name", example = "DISCOUNT_PERCENTAGE")
    private String benefitName;

    @Schema(description = "Resolved configuration key-value pairs", example = "{\"percentage\": \"10\", \"maxDiscount\": \"5000\"}")
    private Map<String, Object> config;
}