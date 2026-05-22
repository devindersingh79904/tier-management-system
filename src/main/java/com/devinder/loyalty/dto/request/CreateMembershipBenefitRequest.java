package com.devinder.loyalty.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a membership benefit")
public class CreateMembershipBenefitRequest {

    @NotBlank(message = "Benefit name is required")
    @Size(min = 2, max = 100, message = "Benefit name must be between 2 and 100 characters")
    @Schema(description = "Name of the benefit", example = "Free Shipping", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Schema(description = "Description of the benefit", example = "Provides free shipping on all orders without minimum spend")
    private String description;
}
