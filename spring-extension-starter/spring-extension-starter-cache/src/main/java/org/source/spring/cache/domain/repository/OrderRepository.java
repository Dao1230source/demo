package org.source.spring.cache.domain.repository;

import org.source.spring.cache.domain.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 订单 Repository
 */
public interface OrderRepository extends JpaRepository<OrderEntity, String> {
}