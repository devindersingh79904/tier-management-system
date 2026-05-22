package com.devinder.loyalty.repository;

import com.devinder.loyalty.entity.MembershipBenefit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MembershipBenefitRepository extends JpaRepository<MembershipBenefit, String> {
    Optional<MembershipBenefit> findByName(String name);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, String id);
    Page<MembershipBenefit> findByIsActiveTrue(Pageable pageable);
}
