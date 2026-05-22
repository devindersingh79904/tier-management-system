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
@Schema(description = "Request body for updating a benefit configuration")
public class UpdateBenefitConfigurationRequest {

    @NotBlank(message = "Configuration JSON is required")
    @Schema(description = "Valid JSON representation of the configuration settings", example = "{\"discountPercent\": 15, \"maxCap\": 750}", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configurationJson;

    @NotNull(message = "Active status is required")
    @Schema(description = "Flag indicating whether this configuration is active", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isActive;
}
