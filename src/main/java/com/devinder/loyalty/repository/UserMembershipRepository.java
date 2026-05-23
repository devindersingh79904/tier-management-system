package com.devinder.loyalty.repository;

import com.devinder.loyalty.entity.UserMembership;
import com.devinder.loyalty.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, String> {
    boolean existsByMembershipTierIdAndStatus(String tierId, MembershipStatus status);
    boolean existsByMembershipPlanIdAndStatus(String planId, MembershipStatus status);
    boolean existsByUserIdAndStatus(String userId, MembershipStatus status);
    List<UserMembership> findByMembershipTierNameStartingWith(String prefix);
    List<UserMembership> findByUserId(String userId);
    List<UserMembership> findByUserIdAndStatus(String userId, MembershipStatus status);
    List<UserMembership> findByStatus(MembershipStatus status);
    List<UserMembership> findByEndDateBeforeAndStatus(Instant now, MembershipStatus status);
    List<UserMembership> findByAutoRenewAndEndDateBetweenAndStatus(boolean autoRenew, Instant start, Instant end, MembershipStatus status);
}

