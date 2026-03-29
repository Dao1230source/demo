package org.source.spring.cache.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.source.spring.cache.configure.CacheInJvm;
import org.source.spring.cache.configure.CacheInRedis;
import org.source.spring.cache.configure.ConfigureCache;
import org.source.spring.cache.domain.entity.OrderEntity;
import org.source.spring.cache.domain.repository.OrderRepository;
import org.source.spring.cache.strategy.PartialCacheStrategyEnum;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单服务 - 测试事务与缓存集成
 */
@AllArgsConstructor
@Slf4j
@Service
public class OrderService {
    private final OrderRepository orderRepository;

    private final AtomicInteger callCount = new AtomicInteger(0);

    @ConfigureCache(
            cacheNames = "order",
            partialCacheStrategy = PartialCacheStrategyEnum.DISTRUST,
            cacheInRedis = @CacheInRedis(enable = false),
            cacheInJvm = @CacheInJvm(enable = true)
    )
    @Transactional
    public OrderEntity getOrderById(String id) {
        callCount.incrementAndGet();
        log.debug("Fetching order by id: {}", id);
        return this.orderRepository.findById(id).orElse(null);
    }

    @ConfigureCache(
            cacheNames = "orders",
            key = "#ids",
            cacheKeySpEl = "#R.id",
            partialCacheStrategy = PartialCacheStrategyEnum.PARTIAL_TRUST,
            cacheInRedis = @CacheInRedis(enable = false),
            cacheInJvm = @CacheInJvm(enable = true)
    )
    @Transactional
    public List<OrderEntity> getOrdersByIds(Collection<String> ids) {
        callCount.incrementAndGet();
        log.debug("Fetching orders by ids: {}", ids);
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return this.orderRepository.findAllById(ids).stream()
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    public OrderEntity createOrder(OrderEntity orderEntity) {
        log.debug("Creating order: {}", orderEntity.getId());
        return this.orderRepository.save(orderEntity);
    }

    @Transactional
    public List<OrderEntity> createOrders(List<OrderEntity> orderEntities) {
        log.debug("Creating {} orders", orderEntities.size());
        return this.orderRepository.saveAll(orderEntities);
    }

    @Transactional
    @CacheEvict(cacheNames = "order", key = "#id")
    public OrderEntity updateOrder(String id, OrderEntity orderEntity) {
        log.debug("Updating order: {}", id);
        orderEntity.setId(id);
        return this.orderRepository.save(orderEntity);
    }

    @Transactional
    @CacheEvict(cacheNames = "order", allEntries = true)
    public List<OrderEntity> updateOrders(List<OrderEntity> orderEntities) {
        log.debug("Updating {} orders", orderEntities.size());
        return this.orderRepository.saveAll(orderEntities);
    }

    @Transactional
    @CacheEvict(cacheNames = "order", key = "#id")
    public void deleteOrder(String id) {
        log.debug("Deleting order: {}", id);
        this.orderRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(cacheNames = "order", allEntries = true)
    public void deleteOrders(List<String> ids) {
        log.debug("Deleting {} orders", ids.size());
        this.orderRepository.deleteAllById(ids);
    }

    @Transactional
    @CacheEvict(cacheNames = "order", allEntries = true)
    public void clearOrderCache() {
        log.debug("Clearing all order cache");
    }

    public int getCallCount() {
        return callCount.get();
    }

    public void resetCallCount() {
        callCount.set(0);
    }
}