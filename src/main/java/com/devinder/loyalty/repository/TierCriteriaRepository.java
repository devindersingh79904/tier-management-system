package com.devinder.loyalty.repository;

import com.devinder.loyalty.entity.TierCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TierCriteriaRepository extends JpaRepository<TierCriteria, String> {
    boolean existsByMembershipTierIdAndIsActiveTrue(String tierId);
    List<TierCriteria> findByMembershipTierIdAndIsActiveTrue(String tierId);
}
