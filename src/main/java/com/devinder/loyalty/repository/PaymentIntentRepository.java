package com.devinder.loyalty.repository;

import com.devinder.loyalty.entity.PaymentIntent;
import com.devinder.loyalty.enums.PaymentStatus;
import com.devinder.loyalty.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, String> {
    Optional<PaymentIntent> findByIdempotencyKey(String idempotencyKey);
    Page<PaymentIntent> findByUserMembershipUserId(String userId, Pageable pageable);
    Page<PaymentIntent> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);
    Page<PaymentIntent> findByTransactionType(TransactionType transactionType, Pageable pageable);
    Page<PaymentIntent> findByUserMembershipId(String userMembershipId, Pageable pageable);
}

