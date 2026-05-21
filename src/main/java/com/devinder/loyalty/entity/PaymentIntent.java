package com.devinder.loyalty.entity;

import com.devinder.loyalty.enums.PaymentStatus;
import com.devinder.loyalty.enums.TransactionType;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "payment_intents",
    indexes = {
        @Index(name = "idx_payment_intents_idempotency_key", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_payment_intents_transaction_id", columnList = "transaction_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntent extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_membership_id", nullable = false)
    private UserMembership userMembership;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    @NotNull
    @Min(0)
    @Column(name = "amount", nullable = false)
    private Long amount; // stored in paise/cents

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @NotBlank
    @Size(max = 100)
    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "payment_provider", nullable = false, length = 50)
    private String paymentProvider;

    @NotBlank
    @Size(max = 100)
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;
}
