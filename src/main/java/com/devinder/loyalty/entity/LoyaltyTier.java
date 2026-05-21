package com.devinder.loyalty.entity;

import com.devinder.loyalty.enums.TierLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loyalty_tiers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTier extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_level", nullable = false)
    private TierLevel level;

    @Column(name = "minimum_points", nullable = false)
    private Integer minimumPoints;

    @Column(name = "description")
    private String description;
}
