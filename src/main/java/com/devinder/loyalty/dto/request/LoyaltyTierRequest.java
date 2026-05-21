package com.devinder.loyalty.dto.request;

import com.devinder.loyalty.constants.MessageConstants;
import com.devinder.loyalty.enums.TierLevel;
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
public class LoyaltyTierRequest {

    @NotBlank(message = MessageConstants.VAL_TIER_NAME_REQUIRED)
    @Size(max = 50, message = MessageConstants.VAL_TIER_NAME_LENGTH)
    private String name;

    @NotNull(message = MessageConstants.VAL_TIER_LEVEL_REQUIRED)
    private TierLevel level;

    @NotNull(message = MessageConstants.VAL_TIER_POINTS_REQUIRED)
    @Min(value = 0, message = MessageConstants.VAL_TIER_POINTS_MIN)
    private Integer minimumPoints;

    @Size(max = 255, message = MessageConstants.VAL_TIER_DESC_LENGTH)
    private String description;
}
