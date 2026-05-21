package com.devinder.loyalty.repository;

import com.devinder.loyalty.entity.MembershipEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipEventRepository extends JpaRepository<MembershipEvent, String> {
}
