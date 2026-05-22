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
@Schema(description = "Request body for updating a tier evaluation criteria")
public class UpdateTierCriteriaRequest {

    @NotBlank(message = "Criteria JSON is required")
    @Schema(description = "Valid JSON representation of the evaluation criteria rules", example = "{\"operator\": \"AND\", \"rules\": [{\"field\": \"totalSpent\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 15000}]}", requiredMode = Schema.RequiredMode.REQUIRED)
    private String criteriaJson;

    @NotNull(message = "Active status is required")
    @Schema(description = "Flag indicating whether this criteria is active", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isActive;
}
