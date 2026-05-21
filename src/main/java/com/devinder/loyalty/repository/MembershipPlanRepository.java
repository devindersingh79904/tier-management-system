package com.devinder.loyalty.repository;

import com.devinder.loyalty.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, String> {
    Optional<MembershipPlan> findByName(String name);
}
