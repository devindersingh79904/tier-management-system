package com.devinder.loyalty.seed;

import com.devinder.loyalty.entity.MembershipEvent;
import com.devinder.loyalty.entity.MembershipPlan;
import com.devinder.loyalty.entity.MembershipTier;
import com.devinder.loyalty.entity.User;
import com.devinder.loyalty.entity.UserMembership;
import com.devinder.loyalty.enums.MembershipEventType;
import com.devinder.loyalty.enums.MembershipStatus;
import com.devinder.loyalty.repository.MembershipEventRepository;
import com.devinder.loyalty.repository.MembershipPlanRepository;
import com.devinder.loyalty.repository.MembershipTierRepository;
import com.devinder.loyalty.repository.UserMembershipRepository;
import com.devinder.loyalty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserMembershipSeeder {

    private final UserMembershipRepository userMembershipRepository;
    private final UserRepository userRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final MembershipTierRepository membershipTierRepository;
    private final MembershipEventRepository membershipEventRepository;

    @Value("${USER_MEMBERSHIP_SEED_ENABLED:false}")
    private boolean enabled;

    public void seed() {
        if (!enabled) {
            log.info("Skipping user membership seeding because USER_MEMBERSHIP_SEED_ENABLED=false");
            return;
        }

        if (userMembershipRepository.count() > 0) {
            log.info("Skipping user membership seeding because memberships already exist.");
            return;
        }

        log.info("User membership seeding started...");

        Optional<MembershipPlan> monthlyOpt = membershipPlanRepository.findByName("Monthly Plan");
        Optional<MembershipPlan> quarterlyOpt = membershipPlanRepository.findByName("Quarterly Plan");
        Optional<MembershipPlan> yearlyOpt = membershipPlanRepository.findByName("Yearly Plan");

        Optional<MembershipTier> silverOpt = membershipTierRepository.findByName("SILVER");
        Optional<MembershipTier> goldOpt = membershipTierRepository.findByName("GOLD");
        Optional<MembershipTier> platinumOpt = membershipTierRepository.findByName("PLATINUM");

        List<UserMembership> memberships = new ArrayList<>();
        Instant now = Instant.now();

        // 1. Rahul Sharma -> PLATINUM + Yearly
        userRepository.findByMobileNumber("769655537").ifPresent(user -> {
            yearlyOpt.ifPresent(plan -> {
                platinumOpt.ifPresent(tier -> {
                    memberships.add(UserMembership.builder()
                            .user(user)
                            .membershipPlan(plan)
                            .membershipTier(tier)
                            .status(MembershipStatus.ACTIVE)
                            .startDate(now.minus(30, ChronoUnit.DAYS))
                            .endDate(now.plus(335, ChronoUnit.DAYS))
                            .purchasedPrice(plan.getBasePrice())
                            .discountAmount(0L)
                            .finalPrice(plan.getBasePrice())
                            .autoRenew(true)
                            .build());
                });
            });
        });

        // 2. Aman Verma -> GOLD + Quarterly
        userRepository.findByMobileNumber("769655538").ifPresent(user -> {
            quarterlyOpt.ifPresent(plan -> {
                goldOpt.ifPresent(tier -> {
                    memberships.add(UserMembership.builder()
                            .user(user)
                            .membershipPlan(plan)
                            .membershipTier(tier)
                            .status(MembershipStatus.ACTIVE)
                            .startDate(now.minus(15, ChronoUnit.DAYS))
                            .endDate(now.plus(75, ChronoUnit.DAYS))
                            .purchasedPrice(plan.getBasePrice())
                            .discountAmount(2000L) // ₹20 discount
                            .finalPrice(plan.getBasePrice() - 2000L)
                            .autoRenew(false)
                            .build());
                });
            });
        });

        // 3. Priya Singh -> SILVER + Monthly
        userRepository.findByMobileNumber("769655539").ifPresent(user -> {
            monthlyOpt.ifPresent(plan -> {
                silverOpt.ifPresent(tier -> {
                    memberships.add(UserMembership.builder()
                            .user(user)
                            .membershipPlan(plan)
                            .membershipTier(tier)
                            .status(MembershipStatus.ACTIVE)
                            .startDate(now.minus(5, ChronoUnit.DAYS))
                            .endDate(now.plus(25, ChronoUnit.DAYS))
                            .purchasedPrice(plan.getBasePrice())
                            .discountAmount(0L)
                            .finalPrice(plan.getBasePrice())
                            .autoRenew(true)
                            .build());
                });
            });
        });

        // 4. Neha Kapoor -> GOLD + Monthly
        userRepository.findByMobileNumber("769655540").ifPresent(user -> {
            monthlyOpt.ifPresent(plan -> {
                goldOpt.ifPresent(tier -> {
                    memberships.add(UserMembership.builder()
                            .user(user)
                            .membershipPlan(plan)
                            .membershipTier(tier)
                            .status(MembershipStatus.ACTIVE)
                            .startDate(now.minus(10, ChronoUnit.DAYS))
                            .endDate(now.plus(20, ChronoUnit.DAYS))
                            .purchasedPrice(plan.getBasePrice())
                            .discountAmount(0L)
                            .finalPrice(plan.getBasePrice())
                            .autoRenew(true)
                            .build());
                });
            });
        });

        // 5. Arjun Patel -> SILVER + Monthly
        userRepository.findByMobileNumber("769655541").ifPresent(user -> {
            monthlyOpt.ifPresent(plan -> {
                silverOpt.ifPresent(tier -> {
                    memberships.add(UserMembership.builder()
                            .user(user)
                            .membershipPlan(plan)
                            .membershipTier(tier)
                            .status(MembershipStatus.ACTIVE)
                            .startDate(now.minus(2, ChronoUnit.DAYS))
                            .endDate(now.plus(28, ChronoUnit.DAYS))
                            .purchasedPrice(plan.getBasePrice())
                            .discountAmount(1000L) // ₹10 discount
                            .finalPrice(plan.getBasePrice() - 1000L)
                            .autoRenew(false)
                            .build());
                });
            });
        });

        if (!memberships.isEmpty()) {
            List<UserMembership> savedMemberships = userMembershipRepository.saveAll(memberships);
            
            // Seed audit events for each membership
            List<MembershipEvent> events = new ArrayList<>();
            for (UserMembership membership : savedMemberships) {
                events.add(MembershipEvent.builder()
                        .userMembership(membership)
                        .eventType(MembershipEventType.SUBSCRIBED)
                        .oldValue(null)
                        .newValue(membership.getMembershipPlan().getName() + " (" + membership.getMembershipTier().getName() + ")")
                        .reason("Initial subscription and registration")
                        .build());
            }
            membershipEventRepository.saveAll(events);
        }

        log.info("User membership seeding completed.");
    }
}
