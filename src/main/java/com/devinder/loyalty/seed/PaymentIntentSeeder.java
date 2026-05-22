package com.devinder.loyalty.seed;

import com.devinder.loyalty.entity.PaymentIntent;
import com.devinder.loyalty.entity.UserMembership;
import com.devinder.loyalty.enums.PaymentMethod;
import com.devinder.loyalty.enums.PaymentStatus;
import com.devinder.loyalty.enums.TransactionType;
import com.devinder.loyalty.repository.PaymentIntentRepository;
import com.devinder.loyalty.repository.UserMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentIntentSeeder {

    private final PaymentIntentRepository paymentIntentRepository;
    private final UserMembershipRepository userMembershipRepository;

    @Value("${PAYMENT_INTENT_SEED_ENABLED:false}")
    private boolean enabled;

    public void seed() {
        if (!enabled) {
            log.info("Skipping payment intent seeding because PAYMENT_INTENT_SEED_ENABLED=false");
            return;
        }

        if (paymentIntentRepository.count() > 0) {
            log.info("Skipping payment intent seeding because payment intents already exist.");
            return;
        }

        log.info("Payment intent seeding started...");

        List<UserMembership> memberships = userMembershipRepository.findAll();
        List<PaymentIntent> paymentIntents = new ArrayList<>();

        int index = 0;
        for (UserMembership membership : memberships) {
            String provider = (index % 2 == 0) ? "STRIPE" : "RAZORPAY";
            String txPrefix = (index % 2 == 0) ? "ch_" : "pay_";
            
            paymentIntents.add(PaymentIntent.builder()
                    .userMembership(membership)
                    .transactionType(TransactionType.PAYMENT)
                    .amount(membership.getFinalPrice())
                    .paymentStatus(PaymentStatus.SUCCESS)
                    .transactionId(txPrefix + UUID.randomUUID().toString().replace("-", "").substring(0, 14))
                    .paymentProvider(provider)
                    .idempotencyKey(UUID.randomUUID().toString())
                    .paymentMethod(PaymentMethod.CARD)
                    .build());
            
            index++;
        }

        if (!paymentIntents.isEmpty()) {
            paymentIntentRepository.saveAll(paymentIntents);
        }

        log.info("Payment intent seeding completed.");
    }
}
