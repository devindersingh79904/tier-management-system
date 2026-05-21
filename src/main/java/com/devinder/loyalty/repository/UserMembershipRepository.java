package com.devinder.loyalty.repository;

import com.devinder.loyalty.entity.UserMembership;
import com.devinder.loyalty.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, String> {
    boolean existsByMembershipTierIdAndStatus(String tierId, MembershipStatus status);
    List<UserMembership> findByMembershipTierNameStartingWith(String prefix);
}

