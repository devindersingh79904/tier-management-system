package com.devinder.loyalty.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tier_criteria")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierCriteria extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_tier_id", nullable = false)
    private MembershipTier membershipTier;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "criteria_json", columnDefinition = "jsonb")
    private String criteriaJson;

    @NotNull
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
