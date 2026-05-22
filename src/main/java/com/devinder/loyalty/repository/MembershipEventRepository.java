package com.devinder.loyalty.repository;

import com.devinder.loyalty.entity.MembershipEvent;
import com.devinder.loyalty.enums.MembershipEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipEventRepository extends JpaRepository<MembershipEvent, String> {
    Page<MembershipEvent> findByUserMembershipId(String userMembershipId, Pageable pageable);
    Page<MembershipEvent> findByUserMembershipUserId(String userId, Pageable pageable);
    Page<MembershipEvent> findByEventType(MembershipEventType eventType, Pageable pageable);
}

