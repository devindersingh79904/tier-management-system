package com.devinder.loyalty.service.impl;

import com.devinder.loyalty.dto.request.CreatePaymentIntentRequest;
import com.devinder.loyalty.dto.request.RefundPaymentRequest;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.dto.response.PaymentIntentResponse;
import com.devinder.loyalty.entity.MembershipEvent;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.entity.PaymentIntent;
import com.devinder.loyalty.entity.User;
import com.devinder.loyalty.entity.UserMembership;
import com.devinder.loyalty.enums.MembershipEventType;
import com.devinder.loyalty.enums.MembershipStatus;
import com.devinder.loyalty.enums.PaymentStatus;
import com.devinder.loyalty.enums.TransactionType;
import com.devinder.loyalty.exception.ConflictException;
import com.devinder.loyalty.exception.ResourceNotFoundException;
import com.devinder.loyalty.exception.UnauthorizedException;
import com.devinder.loyalty.mapper.PaymentIntentMapper;
import com.devinder.loyalty.repository.MembershipEventRepository;
import com.devinder.loyalty.repository.PaymentIntentRepository;
import com.devinder.loyalty.repository.UserMembershipRepository;
import com.devinder.loyalty.repository.UserRepository;
import com.devinder.loyalty.service.PaymentIntentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentIntentServiceImpl implements PaymentIntentService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final UserMembershipRepository userMembershipRepository;
    private final UserRepository userRepository;
    private final MembershipEventRepository membershipEventRepository;
    private final PaymentIntentMapper paymentIntentMapper;

    @Override
    @Transactional
    public PaymentIntentResponse createPayment(CreatePaymentIntentRequest request, String defaultUsername) {
        log.info("Attempting to create payment intent with idempotencyKey: {}", request.getIdempotencyKey());

        // Idempotency check: duplicate key returns existing payment
        Optional<PaymentIntent> existingOpt = paymentIntentRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingOpt.isPresent()) {
            log.info("Duplicate idempotency key detected: {}. Returning existing payment.", request.getIdempotencyKey());
            return paymentIntentMapper.toResponse(existingOpt.get());
        }

        UserMembership membership = userMembershipRepository.findById(request.getUserMembershipId())
                .orElseThrow(() -> new ResourceNotFoundException("User membership not found with ID: " + request.getUserMembershipId()));

        // Security check: Never trust userId from request body if defaultUsername is provided
        if (defaultUsername != null) {
            User user = userRepository.findByMobileNumber(defaultUsername)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with mobile: " + defaultUsername));
            if (!membership.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedException("Authenticated user is not authorized to make payment for this membership");
            }
        }

        // Business Rule: only ACTIVE memberships accept payments
        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            log.warn("Cannot process payment for inactive membership. Current status: {}", membership.getStatus());
            throw new ConflictException("Cannot process payment for a membership that is not ACTIVE");
        }

        PaymentIntent paymentIntent = PaymentIntent.builder()
                .userMembership(membership)
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .paymentStatus(PaymentStatus.SUCCESS)
                .transactionId(request.getTransactionId())
                .paymentProvider(request.getPaymentProvider())
                .idempotencyKey(request.getIdempotencyKey())
                .paymentMethod(request.getPaymentMethod())
                .build();

        PaymentIntent saved = paymentIntentRepository.save(paymentIntent);
        log.info("Payment intent created successfully. ID: {}, status: SUCCESS", saved.getId());

        // Business Rule: RENEWAL updates the membership dates and logs RENEWED event
        if (request.getTransactionType() == TransactionType.RENEWAL) {
            Instant oldEndDate = membership.getEndDate();
            MembershipPlan plan = membership.getMembershipPlan();
            Instant newEndDate = calculateEndDate(oldEndDate, plan.getDuration(), plan.getDurationUnit());
            membership.setEndDate(newEndDate);
            userMembershipRepository.save(membership);

            log.info("Membership renewed via payment. Old end date: {}, new end date: {}", oldEndDate, newEndDate);
            saveMembershipEvent(membership, MembershipEventType.RENEWED, oldEndDate.toString(), newEndDate.toString(), "Membership renewed via payment");
        }

        return paymentIntentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentIntentResponse getPaymentById(String id) {
        log.info("Fetching payment intent by ID: {}", id);
        PaymentIntent payment = paymentIntentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment intent not found with ID: " + id));
        return paymentIntentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentIntentResponse> getAllPayments(Pageable pageable) {
        log.info("Fetching all payment intents paginated. Pageable: {}", pageable);
        Page<PaymentIntent> page = paymentIntentRepository.findAll(pageable);
        return PageResponse.from(page.map(paymentIntentMapper::toResponse));
    }

    @Override
    @Transactional
    public PaymentIntentResponse refundPayment(String id, RefundPaymentRequest request) {
        log.info("Attempting to refund payment intent: {}", id);
        PaymentIntent payment = paymentIntentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment intent not found with ID: " + id));

        // Business Rule: prevent double refund
        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            log.warn("Double refund attempt for payment ID: {}", id);
            throw new ConflictException("Payment is already refunded");
        }

        // Business Rule: refund only SUCCESS payments
        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            log.warn("Refund attempted on non-SUCCESS payment status: {}", payment.getPaymentStatus());
            throw new ConflictException("Only successful payments can be refunded");
        }

        // Mark payment as REFUNDED
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        PaymentIntent saved = paymentIntentRepository.save(payment);
        log.info("Payment intent marked as REFUNDED: {}", saved.getId());

        // Create transaction entry for the refund itself
        PaymentIntent refundTx = PaymentIntent.builder()
                .userMembership(payment.getUserMembership())
                .transactionType(TransactionType.REFUND)
                .amount(payment.getAmount())
                .paymentStatus(PaymentStatus.SUCCESS)
                .transactionId("ref_" + payment.getTransactionId())
                .paymentProvider(payment.getPaymentProvider())
                .idempotencyKey("ref_" + payment.getIdempotencyKey())
                .paymentMethod(payment.getPaymentMethod())
                .build();
        paymentIntentRepository.save(refundTx);
        log.info("Refund transaction record created: {}", refundTx.getId());

        return paymentIntentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentIntentResponse> getMyPayments(String username, Pageable pageable) {
        log.info("Fetching payments for user mobile: {} paginated. Pageable: {}", username, pageable);
        User user = userRepository.findByMobileNumber(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with mobile: " + username));
        Page<PaymentIntent> page = paymentIntentRepository.findByUserMembershipUserId(user.getId(), pageable);
        return PageResponse.from(page.map(paymentIntentMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentIntentResponse getMyPaymentById(String id, String username) {
        log.info("Fetching payment intent by ID: {} for user mobile: {}", id, username);
        User user = userRepository.findByMobileNumber(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with mobile: " + username));
        PaymentIntent payment = paymentIntentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment intent not found with ID: " + id));

        if (!payment.getUserMembership().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("User is not authorized to access this payment record");
        }

        return paymentIntentMapper.toResponse(payment);
    }

    private Instant calculateEndDate(Instant start, int duration, com.devinder.loyalty.enums.DurationUnit unit) {
        java.time.ZonedDateTime zdt = start.atZone(java.time.ZoneOffset.UTC);
        switch (unit) {
            case DAY:
                zdt = zdt.plusDays(duration);
                break;
            case MONTH:
                zdt = zdt.plusMonths(duration);
                break;
            case YEAR:
                zdt = zdt.plusYears(duration);
                break;
        }
        return zdt.toInstant();
    }

    private void saveMembershipEvent(UserMembership membership, MembershipEventType type, String oldValue, String newValue, String reason) {
        MembershipEvent event = MembershipEvent.builder()
                .userMembership(membership)
                .eventType(type)
                .oldValue(oldValue)
                .newValue(newValue)
                .reason(reason)
                .build();
        membershipEventRepository.save(event);
        log.info("Membership event created during payment/renewal. EventType: {}, MembershipId: {}", type, membership.getId());
    }
}
