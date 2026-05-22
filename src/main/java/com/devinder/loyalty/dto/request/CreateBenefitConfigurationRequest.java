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
@Schema(description = "Request body for creating a benefit configuration")
public class CreateBenefitConfigurationRequest {

    @NotBlank(message = "Membership benefit ID is required")
    @Schema(description = "UUID of the membership benefit", example = "a2c16e78-bc5a-4712-8822-79015c92c55b", requiredMode = Schema.RequiredMode.REQUIRED)
    private String membershipBenefitId;

    @Schema(description = "Optional UUID of the membership plan if configuring for a plan", example = "3c4fa2d1-e63b-4890-bc4f-4d9298715c0e")
    private String membershipPlanId;

    @Schema(description = "Optional UUID of the membership tier if configuring for a tier", example = "8e9b62a1-0f7d-41a2-b258-39c288921e90")
    private String membershipTierId;

    @NotBlank(message = "Configuration JSON is required")
    @Schema(description = "Valid JSON representation of the configuration settings", example = "{\"discountPercent\": 10, \"maxCap\": 500}", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configurationJson;
}
