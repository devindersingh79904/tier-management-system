package com.devinder.loyalty.entity;

import com.devinder.loyalty.enums.MembershipStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
    name = "user_memberships",
    indexes = {
        @Index(name = "idx_user_memberships_user_id", columnList = "user_id"),
        @Index(name = "idx_user_memberships_status", columnList = "status"),
        @Index(name = "idx_user_memberships_tier_id", columnList = "membership_tier_id"),
        @Index(name = "idx_user_memberships_end_date", columnList = "end_date")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMembership extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_plan_id", nullable = false)
    private MembershipPlan membershipPlan;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_tier_id", nullable = false)
    private MembershipTier membershipTier;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MembershipStatus status;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @NotNull
    @Min(0)
    @Column(name = "purchased_price", nullable = false)
    private Long purchasedPrice; // stored in paise/cents

    @NotNull
    @Min(0)
    @Column(name = "discount_amount", nullable = false)
    private Long discountAmount; // stored in paise/cents

    @NotNull
    @Min(0)
    @Column(name = "final_price", nullable = false)
    private Long finalPrice; // stored in paise/cents

    @NotNull
    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew;
}
