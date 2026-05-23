package com.devinder.loyalty.repository;

import com.devinder.loyalty.entity.Order;
import com.devinder.loyalty.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status AND o.orderDate >= :start AND o.orderDate < :end")
    long countByUserIdAndStatusAndOrderDateBetween(
            @Param("userId") String userId,
            @Param("status") OrderStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user.id = :userId AND o.status = :status AND o.orderDate >= :start AND o.orderDate < :end")
    long sumTotalAmountByUserIdAndStatusAndOrderDateBetween(
            @Param("userId") String userId,
            @Param("status") OrderStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}