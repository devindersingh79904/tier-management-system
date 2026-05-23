package com.devinder.loyalty.scheduler;

import com.devinder.loyalty.service.UserMembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class MembershipScheduler {

    private final UserMembershipService userMembershipService;

    /**
     * Runs daily at midnight to expire memberships past their end date.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    public void expireMemberships() {
        log.info("Scheduler triggered: expireMemberships");
        userMembershipService.expireMemberships();
    }

    /**
     * Runs daily at 1 AM UTC to auto-renew memberships with autoRenew enabled.
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "UTC")
    public void autoRenewMemberships() {
        log.info("Scheduler triggered: autoRenewMemberships");
        userMembershipService.autoRenewMemberships();
    }

    /**
     * Runs monthly on the 1st at 2 AM UTC to evaluate and adjust membership tiers.
     */
    @Scheduled(cron = "0 0 2 1 * *", zone = "UTC")
    public void evaluateTiers() {
        log.info("Scheduler triggered: evaluateTiers");
        userMembershipService.evaluateTiers();
    }
}