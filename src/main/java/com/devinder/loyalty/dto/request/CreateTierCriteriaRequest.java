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
@Schema(description = "Request body for creating a tier evaluation criteria")
public class CreateTierCriteriaRequest {

    @NotBlank(message = "Membership tier ID is required")
    @Schema(description = "UUID of the membership tier", example = "a2c16e78-bc5a-4712-8822-79015c92c55b", requiredMode = Schema.RequiredMode.REQUIRED)
    private String membershipTierId;

    @NotBlank(message = "Criteria JSON is required")
    @Schema(description = "Valid JSON representation of the evaluation criteria rules", example = "{\"operator\": \"AND\", \"rules\": [{\"field\": \"totalSpent\", \"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": 10000}]}", requiredMode = Schema.RequiredMode.REQUIRED)
    private String criteriaJson;
}
