package com.devinder.loyalty.entity;

import com.devinder.loyalty.enums.MembershipEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "membership_events",
    indexes = {
        @Index(name = "idx_membership_events_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipEvent extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_membership_id", nullable = false)
    private UserMembership userMembership;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private MembershipEventType eventType;

    @Size(max = 1000)
    @Column(name = "old_value", length = 1000)
    private String oldValue;

    @Size(max = 1000)
    @Column(name = "new_value", length = 1000)
    private String newValue;

    @Size(max = 255)
    @Column(name = "reason", length = 255)
    private String reason;
}
