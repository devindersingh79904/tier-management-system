package com.devinder.loyalty.service.impl;

import com.devinder.loyalty.dto.request.CancelMembershipRequest;
import com.devinder.loyalty.dto.request.CreateUserMembershipRequest;
import com.devinder.loyalty.dto.request.DowngradeMembershipRequest;
import com.devinder.loyalty.dto.request.UpgradeMembershipRequest;
import com.devinder.loyalty.dto.response.PageResponse;
import com.devinder.loyalty.dto.response.UserMembershipResponse;
import com.devinder.loyalty.entity.MembershipEvent;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.entity.User;
import com.devinder.loyalty.entity.UserMembership;
import com.devinder.loyalty.enums.MembershipEventType;
import com.devinder.loyalty.enums.MembershipStatus;
import com.devinder.loyalty.enums.OrderStatus;
import com.devinder.loyalty.exception.ConflictException;
import com.devinder.loyalty.exception.ResourceNotFoundException;
import com.devinder.loyalty.mapper.UserMembershipMapper;
import com.devinder.loyalty.repository.MembershipEventRepository;
import com.devinder.loyalty.repository.MembershipPlanRepository;
import com.devinder.loyalty.repository.MembershipTierRepository;
import com.devinder.loyalty.repository.OrderRepository;
import com.devinder.loyalty.repository.UserMembershipRepository;
import com.devinder.loyalty.repository.UserRepository;
import com.devinder.loyalty.service.TierEvaluationService;
import com.devinder.loyalty.service.UserMembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMembershipServiceImpl implements UserMembershipService {

    private final UserMembershipRepository userMembershipRepository;
    private final UserRepository userRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final MembershipTierRepository membershipTierRepository;
    private final MembershipEventRepository membershipEventRepository;
    private final OrderRepository orderRepository;
    private final TierEvaluationService tierEvaluationService;
    private final UserMembershipMapper userMembershipMapper;

    @Override
    @Transactional
    public UserMembershipResponse createMembership(CreateUserMembershipRequest request, String defaultUsername) {
        log.info("Attempting to create membership. Request: {}, defaultUser: {}", request, defaultUsername);

        User user;
        if (request.getUserId() != null && !request.getUserId().trim().isEmpty()) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        } else {
            user = userRepository.findByMobileNumber(defaultUsername)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with mobile: " + defaultUsername));
        }

        MembershipPlan plan = membershipPlanRepository.findById(request.getMembershipPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with id: " + request.getMembershipPlanId()));

        MembershipTier tier = membershipTierRepository.findById(request.getMembershipTierId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership tier not found with id: " + request.getMembershipTierId()));

        // Active state validations
        if (!Boolean.TRUE.equals(plan.getIsActive())) {
            throw new ConflictException("Cannot subscribe to an inactive membership plan");
        }
        if (!Boolean.TRUE.equals(tier.getIsActive())) {
            throw new ConflictException("Cannot subscribe to an inactive membership tier");
        }

        // Overlapping active membership check (serves as creation idempotency check)
        boolean activeExists = userMembershipRepository.existsByUserIdAndStatus(user.getId(), MembershipStatus.ACTIVE);
        if (activeExists) {
            throw new ConflictException("User already has an active membership subscription");
        }

        Long purchasedPrice = plan.getBasePrice();
        Long discountAmount = 0L;
        Long finalPrice = purchasedPrice - discountAmount;

        Instant startDate = Instant.now();
        Instant endDate = calculateEndDate(startDate, plan.getDuration(), plan.getDurationUnit());

        UserMembership membership = UserMembership.builder()
                .user(user)
                .membershipPlan(plan)
                .membershipTier(tier)
                .status(MembershipStatus.ACTIVE)
                .startDate(startDate)
                .endDate(endDate)
                .purchasedPrice(purchasedPrice)
                .discountAmount(discountAmount)
                .finalPrice(finalPrice)
                .autoRenew(request.getAutoRenew())
                .build();

        UserMembership saved = userMembershipRepository.save(membership);

        log.info("Membership created successfully. userId: {}, membershipId: {}, planId: {}, tierId: {}", 
                user.getId(), saved.getId(), plan.getId(), tier.getId());

        saveMembershipEvent(saved, MembershipEventType.SUBSCRIBED, null, tier.getName(), "Initial subscription");

        return userMembershipMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserMembershipResponse getMembershipById(String id) {
        log.info("Fetching membership details. membershipId: {}", id);
        UserMembership membership = userMembershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + id));
        return userMembershipMapper.toResponse(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserMembershipResponse> getAllMemberships(Pageable pageable) {
        log.info("Fetching all memberships paginated");
        return PageResponse.from(userMembershipRepository.findAll(pageable)
                .map(userMembershipMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserMembershipResponse> getAllMemberships() {
        log.info("Fetching all memberships");
        return userMembershipRepository.findAll().stream()
                .map(userMembershipMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserMembershipResponse upgradeMembership(String id, UpgradeMembershipRequest request) {
        log.info("Attempting to upgrade membership. membershipId: {}, targetTierId: {}", id, request.getMembershipTierId());

        UserMembership membership = userMembershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + id));

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new ConflictException("Cannot change tier of a membership that is not ACTIVE");
        }

        MembershipTier newTier = membershipTierRepository.findById(request.getMembershipTierId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership tier not found with id: " + request.getMembershipTierId()));

        if (!Boolean.TRUE.equals(newTier.getIsActive())) {
            throw new ConflictException("Cannot change to an inactive membership tier");
        }

        if (newTier.getPriority() <= membership.getMembershipTier().getPriority()) {
            throw new ConflictException("Target tier must have a higher priority than current tier for upgrade");
        }

        return changeTier(membership, newTier, MembershipEventType.UPGRADED);
    }

    @Override
    @Transactional
    public UserMembershipResponse downgradeMembership(String id, DowngradeMembershipRequest request) {
        log.info("Attempting to downgrade membership. membershipId: {}, targetTierId: {}", id, request.getMembershipTierId());

        UserMembership membership = userMembershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + id));

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new ConflictException("Cannot change tier of a membership that is not ACTIVE");
        }

        MembershipTier newTier = membershipTierRepository.findById(request.getMembershipTierId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership tier not found with id: " + request.getMembershipTierId()));

        if (!Boolean.TRUE.equals(newTier.getIsActive())) {
            throw new ConflictException("Cannot change to an inactive membership tier");
        }

        if (newTier.getPriority() >= membership.getMembershipTier().getPriority()) {
            throw new ConflictException("Target tier must have a lower priority than current tier for downgrade");
        }

        return changeTier(membership, newTier, MembershipEventType.DOWNGRADED);
    }

    private UserMembershipResponse changeTier(UserMembership membership, MembershipTier newTier, MembershipEventType eventType) {
        String oldTierName = membership.getMembershipTier().getName();
        membership.setMembershipTier(newTier);
        UserMembership saved = userMembershipRepository.save(membership);

        log.info("Membership tier changed. userId: {}, membershipId: {}, oldTier: {}, newTier: {}, eventType: {}", 
                membership.getUser().getId(), saved.getId(), oldTierName, newTier.getName(), eventType);

        saveMembershipEvent(saved, eventType, oldTierName, newTier.getName(),
                eventType == MembershipEventType.UPGRADED ? "Membership upgraded" : "Membership downgraded");

        return userMembershipMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserMembershipResponse cancelMembership(String id, CancelMembershipRequest request) {
        log.info("Attempting to cancel membership. membershipId: {}, reason: {}", id, request.getReason());

        UserMembership membership = userMembershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + id));

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new ConflictException("Cannot cancel a membership that is not ACTIVE");
        }

        membership.setStatus(MembershipStatus.CANCELLED);
        UserMembership saved = userMembershipRepository.save(membership);

        log.info("Membership cancelled. userId: {}, membershipId: {}", membership.getUser().getId(), saved.getId());

        saveMembershipEvent(saved, MembershipEventType.CANCELLED, MembershipStatus.ACTIVE.name(), MembershipStatus.CANCELLED.name(), request.getReason());

        return userMembershipMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void expireMemberships() {
        log.info("Running automatic membership expiration routine");
        Instant now = Instant.now();
        List<UserMembership> expiredMemberships = userMembershipRepository.findByEndDateBeforeAndStatus(now, MembershipStatus.ACTIVE);

        for (UserMembership membership : expiredMemberships) {
            log.info("Expiring membership. userId: {}, membershipId: {}", membership.getUser().getId(), membership.getId());
            membership.setStatus(MembershipStatus.EXPIRED);
            UserMembership saved = userMembershipRepository.save(membership);
            saveMembershipEvent(saved, MembershipEventType.EXPIRED, MembershipStatus.ACTIVE.name(), MembershipStatus.EXPIRED.name(), "Automatic expiration");
        }
    }

    @Override
    @Transactional
    public void autoRenewMemberships() {
        log.info("Running automatic membership renewal routine");
        Instant now = Instant.now();
        // Find active memberships with autoRenew enabled that are expiring within the next day
        List<UserMembership> renewals = userMembershipRepository.findByAutoRenewAndEndDateBetweenAndStatus(
                true, now, now.plusSeconds(86400), MembershipStatus.ACTIVE);

        for (UserMembership membership : renewals) {
            log.info("Auto-renewing membership. userId: {}, membershipId: {}", membership.getUser().getId(), membership.getId());
            MembershipPlan plan = membership.getMembershipPlan();
            Instant newStartDate = membership.getEndDate();
            Instant newEndDate = calculateEndDate(newStartDate, plan.getDuration(), plan.getDurationUnit());

            membership.setStartDate(newStartDate);
            membership.setEndDate(newEndDate);
            UserMembership saved = userMembershipRepository.save(membership);
            saveMembershipEvent(saved, MembershipEventType.RENEWED,
                    MembershipStatus.ACTIVE.name(), MembershipStatus.ACTIVE.name(), "Auto-renewal");
        }
    }

    @Override
    @Transactional
    public void evaluateTiers() {
        log.info("Running automatic tier evaluation routine");
        List<UserMembership> activeMemberships = userMembershipRepository.findByStatus(MembershipStatus.ACTIVE);

        for (UserMembership membership : activeMemberships) {
            try {
                String userId = membership.getUser().getId();
                String cohort = membership.getUser().getCohort();

                // Build context with order stats and cohort data
                Map<String, Object> context = buildEvaluationContext(membership);

                // Evaluate eligibility for the current tier's criteria
                String currentTierId = membership.getMembershipTier().getId();
                boolean meetsCurrentCriteria = tierEvaluationService.evaluateEligibility(currentTierId, context);

                if (!meetsCurrentCriteria) {
                    // Check lower tiers for demotion eligibility
                    List<MembershipTier> lowerTiers = membershipTierRepository
                            .findByPriorityLessThanAndIsActiveTrueOrderByPriorityDesc(
                                    membership.getMembershipTier().getPriority());

                    for (MembershipTier lowerTier : lowerTiers) {
                        if (tierEvaluationService.evaluateEligibility(lowerTier.getId(), context)) {
                            membership.setMembershipTier(lowerTier);
                            UserMembership saved = userMembershipRepository.save(membership);
                            saveMembershipEvent(saved, MembershipEventType.DOWNGRADED,
                                    currentTierId, lowerTier.getId(), "Automatic tier evaluation - demotion");
                            log.info("Membership demoted. userId: {}, membershipId: {}, oldTier: {}, newTier: {}",
                                    userId, saved.getId(), currentTierId, lowerTier.getId());
                            break;
                        }
                    }
                } else {
                    // Check higher tiers for promotion eligibility
                    List<MembershipTier> higherTiers = membershipTierRepository
                            .findByPriorityGreaterThanAndIsActiveTrueOrderByPriorityAsc(
                                    membership.getMembershipTier().getPriority());

                    for (MembershipTier higherTier : higherTiers) {
                        if (tierEvaluationService.evaluateEligibility(higherTier.getId(), context)) {
                            membership.setMembershipTier(higherTier);
                            UserMembership saved = userMembershipRepository.save(membership);
                            saveMembershipEvent(saved, MembershipEventType.UPGRADED,
                                    currentTierId, higherTier.getId(), "Automatic tier evaluation - promotion");
                            log.info("Membership promoted. userId: {}, membershipId: {}, oldTier: {}, newTier: {}",
                                    userId, saved.getId(), currentTierId, higherTier.getId());
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error during tier evaluation for membership: {}", membership.getId(), e);
            }
        }
    }

    private Map<String, Object> buildEvaluationContext(UserMembership membership) {
        Map<String, Object> context = new HashMap<>();
        String userId = membership.getUser().getId();

        // Count successful orders in the last 12 months
        Instant now = Instant.now();
        Instant twelveMonthsAgo = now.atZone(ZoneOffset.UTC).minusMonths(12).toInstant();
        long orderCount = orderRepository.countByUserIdAndStatusAndOrderDateBetween(
                userId, OrderStatus.SUCCESSFUL, twelveMonthsAgo, now);
        long totalOrderValue = orderRepository.sumTotalAmountByUserIdAndStatusAndOrderDateBetween(
                userId, OrderStatus.SUCCESSFUL, twelveMonthsAgo, now);

        context.put("orderCount", orderCount);
        context.put("totalOrderValue", totalOrderValue);

        // Add cohort if present
        String cohort = membership.getUser().getCohort();
        if (cohort != null && !cohort.isBlank()) {
            context.put("cohort", cohort);
        }

        return context;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserMembershipResponse> getMyMembershipsHistory(String username) {
        log.info("Fetching membership history for mobile: {}", username);
        User user = userRepository.findByMobileNumber(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with mobile: " + username));
        return userMembershipRepository.findByUserId(user.getId()).stream()
                .map(userMembershipMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserMembershipResponse getMyActiveMembership(String username) {
        log.info("Fetching active membership for mobile: {}", username);
        User user = userRepository.findByMobileNumber(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with mobile: " + username));
        List<UserMembership> active = userMembershipRepository.findByUserIdAndStatus(user.getId(), MembershipStatus.ACTIVE);
        if (active.isEmpty()) {
            throw new ResourceNotFoundException("No active membership found for user");
        }
        return userMembershipMapper.toResponse(active.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserMembershipResponse> getMyMemberships(String username) {
        return getMyMembershipsHistory(username);
    }

    private Instant calculateEndDate(Instant start, int duration, com.devinder.loyalty.enums.DurationUnit unit) {
        ZonedDateTime zdt = start.atZone(ZoneOffset.UTC);
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
    }
}