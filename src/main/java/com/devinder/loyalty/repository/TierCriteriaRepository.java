package com.devinder.loyalty.repository;

import com.devinder.loyalty.entity.TierCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TierCriteriaRepository extends JpaRepository<TierCriteria, String> {
}
