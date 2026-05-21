package com.devinder.loyalty.repository;

import com.devinder.loyalty.entity.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MembershipTierRepository extends JpaRepository<MembershipTier, String> {
    Optional<MembershipTier> findByName(String name);
}
