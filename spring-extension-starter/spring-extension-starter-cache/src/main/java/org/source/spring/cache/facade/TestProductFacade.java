package org.source.spring.cache.facade;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.source.spring.cache.configure.CacheInJvm;
import org.source.spring.cache.configure.CacheInRedis;
import org.source.spring.cache.configure.ConfigureCache;
import org.source.spring.cache.configure.ReturnTypeEnum;
import org.source.spring.cache.facade.out.ProductOut;
import org.source.spring.cache.strategy.PartialCacheStrategyEnum;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试产品服务
 */
@Slf4j
@Service
public class TestProductFacade {

    private final Map<String, ProductOut> productDatabase = new ConcurrentHashMap<>();
    private final AtomicInteger callCount = new AtomicInteger(0);
    private final AtomicInteger evictCallCount = new AtomicInteger(0);

    /**
     * 构造函数，初始化测试数据。
     */
    public TestProductFacade() {
        initTestData();
    }

    /**
     * 初始化测试产品数据（仅用于演示/测试）。
     */
    private void initTestData() {
        productDatabase.put("1", ProductOut.builder().id("1").name("Laptop").price(999.99).build());
        productDatabase.put("2", ProductOut.builder().id("2").name("Mouse").price(29.99).build());
        productDatabase.put("3", ProductOut.builder().id("3").name("Keyboard").price(79.99).build());
        productDatabase.put("4", ProductOut.builder().id("4").name("Monitor").price(299.99).build());
        productDatabase.put("5", ProductOut.builder().id("5").name("Headset").price(149.99).build());
    }

/**
 * 根据产品ID查询单个产品。
 *
 * @param id 产品ID
 * @return ProductOut 若未找到则可能返回 null
 */
    @ConfigureCache(cacheNames = "product", partialCacheStrategy = PartialCacheStrategyEnum.DISTRUST)
    public ProductOut getProductById(String id) {
        callCount.incrementAndGet();
        log.debug("Fetching product by id: {}", id);
        return productDatabase.get(id);
    }

/**
     * 批量根据一组ID查询产品，返回列表形式。
     * 支持部分命中缓存策略（PARTIAL_TRUST）。
     *
     * @param ids 要查询的产品ID集合
     * @return 找到的产品列表（顺序与ids一致，未找到的ID不会出现在结果中）
     */
    @ConfigureCache(
            cacheNames = "products",
            key = "#ids",
            cacheKeySpEl = "#R.id",
            returnType = ReturnTypeEnum.LIST,
            partialCacheStrategy = PartialCacheStrategyEnum.PARTIAL_TRUST
    )
    public List<ProductOut> getProductsByIds(Collection<String> ids) {
        callCount.incrementAndGet();
        log.debug("Fetching products by ids: {}", ids);
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return ids.stream()
                .map(productDatabase::get)
                .filter(Objects::nonNull)
                .toList();
    }

/**
     * 批量根据ID查询，返回ID到ProductOut的映射（Map）。
     *
     * @param ids 要查询的产品ID集合
     * @return 包含找到的产品的 Map，key 为产品ID
     */
    @ConfigureCache(
            cacheNames = "productMap",
            key = "#ids",
            returnType = ReturnTypeEnum.MAP
    )
    public Map<String, ProductOut> getProductMapByIds(Collection<String> ids) {
        callCount.incrementAndGet();
        log.debug("Fetching product map by ids: {}", ids);
        Map<String, ProductOut> result = new HashMap<>();
        if (MapUtils.isNotEmpty(productDatabase) && CollectionUtils.isNotEmpty(ids)) {
            ids.forEach(id -> {
                ProductOut product = productDatabase.get(id);
                if (Objects.nonNull(product)) {
                    result.put(id, product);
                }
            });
        }
        return result;
    }

/**
     * 批量查询（TRUST 策略）：假定缓存数据可信并直接返回已命中的部分结果。
     *
     * @param ids 产品ID集合
     * @return 产品列表
     */
    @ConfigureCache(
            cacheNames = "productsTrust",
            key = "#ids",
            cacheKeySpEl = "#R.id",
            returnType = ReturnTypeEnum.LIST,
            partialCacheStrategy = PartialCacheStrategyEnum.TRUST
    )
    public List<ProductOut> getProductsByIdsWithTrustStrategy(Collection<String> ids) {
        callCount.incrementAndGet();
        log.debug("Fetching products with TRUST strategy by ids: {}", ids);
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return ids.stream()
                .map(productDatabase::get)
                .filter(Objects::nonNull)
                .toList();
    }

/**
     * 批量查询（DISTRUST 策略）：对缓存命中保持谨慎，可能触发额外查询以补全结果。
     *
     * @param ids 产品ID集合
     * @return 产品列表
     */
    @ConfigureCache(
            cacheNames = "productsDistrust",
            key = "#ids",
            cacheKeySpEl = "#R.id",
            returnType = ReturnTypeEnum.LIST,
            partialCacheStrategy = PartialCacheStrategyEnum.DISTRUST
    )
    public List<ProductOut> getProductsByIdsWithDistrustStrategy(Collection<String> ids) {
        callCount.incrementAndGet();
        log.debug("Fetching products with DISTRUST strategy by ids: {}", ids);
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return ids.stream()
                .map(productDatabase::get)
                .filter(Objects::nonNull)
                .toList();
    }

/**
     * 批量查询并以 Set 形式返回结果（去重）。
     *
     * @param ids 产品ID集合
     * @return 产品集合（去重）
     */
    @ConfigureCache(
            cacheNames = "productSet",
            key = "#ids",
            cacheKeySpEl = "#R.id",
            returnType = ReturnTypeEnum.SET
    )
    public Set<ProductOut> getProductsByIdsAsSet(Collection<String> ids) {
        callCount.incrementAndGet();
        log.debug("Fetching products as Set by ids: {}", ids);
        if (CollectionUtils.isEmpty(ids)) {
            return Set.of();
        }
        Set<ProductOut> result = new HashSet<>();
        ids.forEach(id -> {
            ProductOut product = productDatabase.get(id);
            if (Objects.nonNull(product)) {
                result.add(product);
            }
        });
        return result;
    }

/**
 * 获取热门产品（双级缓存：Redis + JVM），Redis TTL 为 3600 秒，JVM 缓存 TTL 为 300 秒。
 *
 * @param id 产品ID
 * @return ProductOut 若未找到则返回 null
 */
    @ConfigureCache(
            cacheNames = "hotProduct",
            key = "#id",
            cacheInRedis = @CacheInRedis(ttl = 3600),
            cacheInJvm = @CacheInJvm(enable = true, ttl = 300, jvmMaxSize = 1000),
            partialCacheStrategy = PartialCacheStrategyEnum.DISTRUST
    )
    public ProductOut getHotProductById(String id) {
        callCount.incrementAndGet();
        log.debug("Fetching hot product by id: {}", id);
        return productDatabase.get(id);
    }

    /**
     * 条件缓存示例：仅当 id 不为空且以 '1' 开头时才使用缓存。
     *
     * @param id 产品ID
     * @return ProductOut 或 null
     */
    @ConfigureCache(
            cacheNames = "conditionalProduct",
            key = "#id",
            condition = "#id != null && #id.startsWith('1')",
            partialCacheStrategy = PartialCacheStrategyEnum.DISTRUST
    )
    public ProductOut getProductWithCondition(String id) {
        callCount.incrementAndGet();
        log.debug("Fetching product with condition by id: {}", id);
        return productDatabase.get(id);
    }

    /**
     * 使用自定义 Redis TTL 的缓存示例，TTL = 60 秒。
     *
     * @param id 产品ID
     * @return ProductOut 或 null
     */
    @ConfigureCache(
            cacheNames = "customTtlProduct",
            key = "#id",
            cacheInRedis = @CacheInRedis(ttl = 60),
            partialCacheStrategy = PartialCacheStrategyEnum.DISTRUST
    )
    public ProductOut getProductWithCustomTtl(String id) {
        callCount.incrementAndGet();
        log.debug("Fetching product with custom TTL by id: {}", id);
        return productDatabase.get(id);
    }

    /**
     * 驱逐指定产品的缓存项（按ID）。
     *
     * @param id 要驱逐的产品ID
     */
    @CacheEvict(cacheNames = "product", key = "#id")
    public void evictProduct(String id) {
        evictCallCount.incrementAndGet();
        log.debug("Evicting product with id: {}", id);
    }

    /**
     * 驱逐产品缓存的所有条目（清空缓存）。
     */
    @CacheEvict(cacheNames = "product", allEntries = true)
    public void evictAllProducts() {
        evictCallCount.incrementAndGet();
        log.debug("Evicting all products");
    }

    /**
     * 查询产品，允许返回 null（当传入 id 为空或数据库中不存在时）。
     *
     * @param id 产品ID
     * @return ProductOut 或 null
     */
    @ConfigureCache(cacheNames = "productNull", partialCacheStrategy = PartialCacheStrategyEnum.DISTRUST)
    public ProductOut getProductOrNull(String id) {
        callCount.incrementAndGet();
        log.debug("Fetching product or null by id: {}", id);
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return productDatabase.get(id);
    }

    /**
     * 仅在 JVM 层缓存的批量查询示例（Redis 被禁用）。
     *
     * @param ids 产品ID集合
     * @return 产品列表
     */
    @ConfigureCache(
            cacheNames = "productsJvmOnly",
            key = "#ids",
            cacheKeySpEl = "#R.id",
            returnType = ReturnTypeEnum.LIST,
            cacheInRedis = @CacheInRedis(enable = false),
            cacheInJvm = @CacheInJvm(enable = true, ttl = 60)
    )
    public List<ProductOut> getProductsJvmOnly(Collection<String> ids) {
        callCount.incrementAndGet();
        log.debug("Fetching products JVM only by ids: {}", ids);
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return ids.stream()
                .map(productDatabase::get)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 获取业务方法被调用的计数（用于测试/监控）。
     *
     * @return 调用次数
     */
    public int getCallCount() {
        return callCount.get();
    }

    /**
     * 重置业务方法调用计数为 0（用于测试/监控）。
     */
    public void resetCallCount() {
        callCount.set(0);
    }

    /**
     * 获取驱逐缓存方法被调用的计数（用于测试/监控）。
     *
     * @return 驱逐调用次数
     */
    public int getEvictCallCount() {
        return evictCallCount.get();
    }

    /**
     * 重置驱逐缓存方法的调用计数为 0（用于测试/监控）。
     */
    public void resetEvictCallCount() {
        evictCallCount.set(0);
    }
}