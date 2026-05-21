package com.devinder.loyalty.dto.response;

import com.devinder.loyalty.enums.TierLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTierResponse {
    private Long id;
    private String name;
    private TierLevel level;
    private Integer minimumPoints;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;
}
