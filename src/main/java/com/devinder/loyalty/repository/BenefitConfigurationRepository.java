package com.devinder.loyalty.repository;

import com.devinder.loyalty.entity.BenefitConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BenefitConfigurationRepository extends JpaRepository<BenefitConfiguration, String> {
}
