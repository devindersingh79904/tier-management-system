package com.devinder.loyalty.repository;

import com.devinder.loyalty.entity.BenefitConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BenefitConfigurationRepository extends JpaRepository<BenefitConfiguration, String> {
    boolean existsByMembershipBenefitIdAndIsActiveTrue(String benefitId);

    @Query("SELECT bc FROM BenefitConfiguration bc WHERE bc.isActive = true AND " +
           "((bc.membershipPlan.id = :planId AND bc.membershipTier.id IS NULL) OR " +
           " (bc.membershipTier.id = :tierId AND bc.membershipPlan.id IS NULL) OR " +
           " (bc.membershipPlan.id = :planId AND bc.membershipTier.id = :tierId))")
    List<BenefitConfiguration> findActiveConfigurations(@Param("planId") String planId, @Param("tierId") String tierId);

    List<BenefitConfiguration> findByMembershipTierIdAndIsActiveTrue(String tierId);
    List<BenefitConfiguration> findByMembershipPlanIdAndIsActiveTrue(String planId);

    @Query("SELECT COUNT(bc) > 0 FROM BenefitConfiguration bc WHERE bc.membershipBenefit.id = :benefitId " +
           "AND ((bc.membershipPlan IS NULL AND :planId IS NULL) OR (bc.membershipPlan.id = :planId AND :planId IS NOT NULL)) " +
           "AND ((bc.membershipTier IS NULL AND :tierId IS NULL) OR (bc.membershipTier.id = :tierId AND :tierId IS NOT NULL)) " +
           "AND bc.isActive = true")
    boolean existsActiveConfig(@Param("benefitId") String benefitId, @Param("planId") String planId, @Param("tierId") String tierId);
}
