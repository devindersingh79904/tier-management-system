package com.devinder.loyalty.repository;

import com.devinder.loyalty.entity.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipTierRepository extends JpaRepository<MembershipTier, String> {
    Optional<MembershipTier> findByName(String name);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, String id);
    boolean existsByPriority(Integer priority);
    boolean existsByPriorityAndIdNot(Integer priority, String id);
    List<MembershipTier> findByPriorityLessThanAndIsActiveTrueOrderByPriorityDesc(Integer priority);
    List<MembershipTier> findByPriorityGreaterThanAndIsActiveTrueOrderByPriorityAsc(Integer priority);
}
