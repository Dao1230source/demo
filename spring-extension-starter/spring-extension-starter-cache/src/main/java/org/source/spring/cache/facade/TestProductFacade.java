package org.source.spring.cache.facade;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.source.spring.cache.configure.CacheInJvm;
import org.source.spring.cache.configure.CacheInRedis;
import org.source.spring.cache.configure.ConfigureCache;
import org.source.spring.cache.configure.ReturnTypeEnum;
import org.source.spring.cache.configure.ShardStrategyEnum;
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
 * 
 * <p>提供各种缓存配置示例，用于演示和测试 @ConfigureCache 注解的功能。</p>
 */
@Slf4j
@Service
public class TestProductFacade {

    private final Map<String, ProductOut> productDatabase = new ConcurrentHashMap<>();
    private final Map<Long, ProductOut> productDatabaseById = new ConcurrentHashMap<>();
    private final AtomicInteger callCount = new AtomicInteger(0);
    private final AtomicInteger evictCallCount = new AtomicInteger(0);

    public TestProductFacade() {
        initTestData();
    }

    private void initTestData() {
        productDatabase.put("1", ProductOut.builder().id("1").name("Laptop").price(999.99).build());
        productDatabase.put("2", ProductOut.builder().id("2").name("Mouse").price(29.99).build());
        productDatabase.put("3", ProductOut.builder().id("3").name("Keyboard").price(79.99).build());
        productDatabase.put("4", ProductOut.builder().id("4").name("Monitor").price(299.99).build());
        productDatabase.put("5", ProductOut.builder().id("5").name("Headset").price(149.99).build());
        for (int i = 1; i <= 100; i++) {
            String id = String.valueOf(i);
            ProductOut product = productDatabase.get(id);
            if (product == null) {
                product = ProductOut.builder().id(id).name("Product-" + i).price(10.0 * i).build();
                productDatabase.put(id, product);
            }
            productDatabaseById.put((long) i, product);
        }
    }

    /**
     * 根据ID查询单个产品
     * 
     * <p><b>缓存配置</b>：cacheNames=product, 策略=DISTRUST（不信任部分缓存）</p>
     * <p><b>数据预期</b>：返回ID对应的产品，若不存在返回null</p>
     * <p><b>Redis结构</b>：String key-value，key格式为 product::id</p>
     *
     * @param id 产品ID
     * @return 产品对象，不存在时返回null
     */
    @ConfigureCache(cacheNames = "product", partialCacheStrategy = PartialCacheStrategyEnum.DISTRUST)
    public ProductOut getProductById(String id) {
        callCount.incrementAndGet();
        log.debug("Fetching product by id: {}", id);
        return productDatabase.get(id);
    }

    /**
     * 批量查询产品（返回List）
     * 
     * <p><b>缓存配置</b>：cacheNames=products, 策略=PARTIAL_TRUST（部分信任），返回类型=LIST</p>
     * <p><b>数据预期</b>：返回存在的产品列表，顺序与输入ids对应</p>
     * <p><b>Redis结构</b>：String key-value，每个产品独立存储</p>
     * <p><b>部分命中行为</b>：命中缓存的部分直接返回，未命中的key触发增量查询</p>
     *
     * @param ids 产品ID集合
     * @return 产品列表
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
     * 批量查询产品（返回Map）
     * 
     * <p><b>缓存配置</b>：cacheNames=productMap, 返回类型=MAP</p>
     * <p><b>数据预期</b>：返回ID到产品的映射Map</p>
     * <p><b>Redis结构</b>：String key-value，每个产品独立存储</p>
     *
     * @param ids 产品ID集合
     * @return ID到产品的Map
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
     * 批量查询产品（TRUST策略）
     * 
     * <p><b>缓存配置</b>：cacheNames=productsTrust, 策略=TRUST（完全信任缓存）</p>
     * <p><b>数据预期</b>：仅返回缓存命中的部分，不触发增量查询</p>
     * <p><b>注意</b>：此策略可能导致数据不完整，适用于对数据一致性要求不高的场景</p>
     *
     * @param ids 产品ID集合
     * @return 缓存命中的产品列表
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
     * 批量查询产品（DISTRUST策略）
     * 
     * <p><b>缓存配置</b>：cacheNames=productsDistrust, 策略=DISTRUST（不信任部分缓存）</p>
     * <p><b>数据预期</b>：若有任何key未命中，则废弃所有缓存结果，重新查询全部</p>
     * <p><b>适用场景</b>：对数据完整性要求严格的场景</p>
     *
     * @param ids 产品ID集合
     * @return 完整的产品列表
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
     * 批量查询产品（返回Set）
     * 
     * <p><b>缓存配置</b>：cacheNames=productSet, 返回类型=SET</p>
     * <p><b>数据预期</b>：返回去重后的产品集合</p>
     *
     * @param ids 产品ID集合
     * @return 去重的产品集合
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
     * 查询热门产品（双级缓存示例）
     * 
     * <p><b>缓存配置</b>：cacheNames=hotProduct, Redis TTL=3600s, JVM TTL=300s, JVM maxSize=1000</p>
     * <p><b>数据预期</b>：优先从JVM缓存获取，未命中则查Redis，再未命中则查数据库</p>
     * <p><b>Redis结构</b>：String key-value</p>
     *
     * @param id 产品ID
     * @return 产品对象
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
     * 条件缓存示例
     * 
     * <p><b>缓存配置</b>：cacheNames=conditionalProduct, condition=#id != null && #id.startsWith('1')</p>
     * <p><b>数据预期</b>：仅当ID以'1'开头时才使用缓存，其他ID直接查询数据库</p>
     *
     * @param id 产品ID
     * @return 产品对象
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
     * 自定义TTL示例
     * 
     * <p><b>缓存配置</b>：cacheNames=customTtlProduct, Redis TTL=60s</p>
     * <p><b>数据预期</b>：缓存60秒后自动过期</p>
     *
     * @param id 产品ID
     * @return 产品对象
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
     * 驱逐单个产品的缓存
     *
     * @param id 产品ID
     */
    @CacheEvict(cacheNames = "product", key = "#id")
    public void evictProduct(String id) {
        evictCallCount.incrementAndGet();
        log.debug("Evicting product with id: {}", id);
    }

    /**
     * 驱逐所有产品缓存
     */
    @CacheEvict(cacheNames = "product", allEntries = true)
    public void evictAllProducts() {
        evictCallCount.incrementAndGet();
        log.debug("Evicting all products");
    }

    /**
     * 查询产品（允许返回null）
     * 
     * <p><b>缓存配置</b>：cacheNames=productNull, 策略=DISTRUST</p>
     * <p><b>数据预期</b>：ID为空或不存在时返回null</p>
     *
     * @param id 产品ID
     * @return 产品对象或null
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
     * 仅JVM缓存的批量查询
     * 
     * <p><b>缓存配置</b>：cacheNames=productsJvmOnly, Redis禁用, JVM启用, TTL=60s</p>
     * <p><b>数据预期</b>：数据仅存储在JVM本地缓存，不写入Redis</p>
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

    // ==================== Redis 分片测试方法 ====================

    /**
     * FIXED_SHARD 分片策略 - Redis HSET 分片存储
     * 
     * <p><b>缓存配置</b>：</p>
     * <ul>
     *   <li>cacheNames = productsFixedShardRedis</li>
     *   <li>shardStrategy = FIXED_SHARD（按hashCode分片）</li>
     *   <li>shardValue = 8（分8个HSET）</li>
     *   <li>JVM缓存禁用</li>
     * </ul>
     * 
     * <p><b>分片计算</b>：shardIndex = Math.abs(key.hashCode()) % 8</p>
     * 
     * <p><b>Redis结构</b>：</p>
     * <pre>
     * Key: productsFixedShardRedis:shard:0 (HSET)
     *   field: "1" → value: serialized(ProductOut)
     *   field: "8" → value: serialized(ProductOut)
     * Key: productsFixedShardRedis:shard:1 (HSET)
     *   field: "2" → value: serialized(ProductOut)
     * ...
     * Key: productsFixedShardRedis:shard:7 (HSET)
     * </pre>
     * 
     * <p><b>数据预期</b>：输入50个ID，分散存储在8个HSET分片中，每个分片约6-7条数据</p>
     *
     * @param ids 产品ID集合（String类型）
     * @return 产品列表
     */
    @ConfigureCache(
            cacheNames = "productsFixedShardRedis",
            key = "#ids",
            cacheKeySpEl = "#R.id",
            partialCacheStrategy = PartialCacheStrategyEnum.PARTIAL_TRUST,
            cacheInRedis = @CacheInRedis(
                    enable = true,
                    shardStrategy = ShardStrategyEnum.FIXED_SHARD,
                    shardValue = 8,
                    valueClasses = {ProductOut.class}
            ),
            cacheInJvm = @CacheInJvm(enable = false)
    )
    public List<ProductOut> getProductsFixedShardRedis(Collection<String> ids) {
        callCount.incrementAndGet();
        log.debug("Fetching products with FIXED_SHARD (Redis), ids: {}", ids.size());
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return ids.stream()
                .map(productDatabase::get)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * FIXED_SIZE 分片策略 - Redis HSET 分片存储（适用于自增主键）
     * 
     * <p><b>缓存配置</b>：</p>
     * <ul>
     *   <li>cacheNames = productsFixedSizeRedis</li>
     *   <li>shardStrategy = FIXED_SIZE（按数值范围分片）</li>
     *   <li>shardValue = 50（每50条数据一个分片）</li>
     *   <li>JVM缓存禁用</li>
     * </ul>
     * 
     * <p><b>分片计算</b>：shardIndex = (id / shardValue) + 1</p>
     * 
     * <p><b>Redis结构</b>：</p>
     * <pre>
     * Key: productsFixedSizeRedis:shard:1 (HSET)
     *   field: "1" → value: serialized(ProductOut for ID 1)
     *   ...
     *   field: "49" → value: serialized(ProductOut for ID 49)
     * Key: productsFixedSizeRedis:shard:2 (HSET)
     *   field: "50" → value: serialized(ProductOut for ID 50)
     *   ...
     *   field: "99" → value: serialized(ProductOut for ID 99)
     * Key: productsFixedSizeRedis:shard:3 (HSET)
     *   field: "100" → value: serialized(ProductOut for ID 100)
     * </pre>
     * 
     * <p><b>数据分布</b>：</p>
     * <ul>
     *   <li>ID 1-49 存入 shard:1（49条）</li>
     *   <li>ID 50-99 存入 shard:2（50条）</li>
     *   <li>ID 100-149 存入 shard:3（50条）</li>
     * </ul>
     * 
     * <p><b>注意</b>：FIXED_SIZE策略要求key必须是Integer或Long类型</p>
     *
     * @param ids 产品ID集合（Long类型，必须是数值）
     * @return 产品列表
     */
    @ConfigureCache(
            cacheNames = "productsFixedSizeRedis",
            key = "#ids",
            cacheKeySpEl = "#R.id",
            partialCacheStrategy = PartialCacheStrategyEnum.PARTIAL_TRUST,
            cacheInRedis = @CacheInRedis(
                    enable = true,
                    shardStrategy = ShardStrategyEnum.FIXED_SIZE,
                    shardValue = 50,
                    valueClasses = {ProductOut.class}
            ),
            cacheInJvm = @CacheInJvm(enable = false)
    )
    public List<ProductOut> getProductsFixedSizeRedis(Collection<Long> ids) {
        callCount.incrementAndGet();
        log.debug("Fetching products with FIXED_SIZE (Redis), ids: {}", ids.size());
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return ids.stream()
                .map(productDatabaseById::get)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 双级缓存 + FIXED_SHARD 分片
     * 
     * <p><b>缓存配置</b>：</p>
     * <ul>
     *   <li>cacheNames = productsTwoLevelShard</li>
     *   <li>shardStrategy = FIXED_SHARD, shardValue = 4（分4个HSET）</li>
     *   <li>JVM缓存启用, TTL=60s</li>
     *   <li>Redis分片存储启用</li>
     * </ul>
     * 
     * <p><b>查询顺序</b>：</p>
     * <ol>
     *   <li>优先从JVM本地缓存获取</li>
     *   <li>JVM未命中则从Redis HSET分片获取</li>
     *   <li>Redis也未命中则查询数据库</li>
     *   <li>查询结果同时写入JVM和Redis</li>
     * </ol>
     * 
     * <p><b>Redis结构</b>：</p>
     * <pre>
     * Key: productsTwoLevelShard:shard:0 ~ :shard:3 (HSET)
     * </pre>
     * 
     * <p><b>数据预期</b>：首次查询触发数据库调用，后续查询优先命中JVM缓存</p>
     *
     * @param ids 产品ID集合（String类型）
     * @return 产品列表
     */
    @ConfigureCache(
            cacheNames = "productsTwoLevelShard",
            key = "#ids",
            cacheKeySpEl = "#R.id",
            partialCacheStrategy = PartialCacheStrategyEnum.PARTIAL_TRUST,
            cacheInRedis = @CacheInRedis(
                    enable = true,
                    shardStrategy = ShardStrategyEnum.FIXED_SHARD,
                    shardValue = 4,
                    valueClasses = {ProductOut.class}
            ),
            cacheInJvm = @CacheInJvm(enable = true, ttl = 60)
    )
    public List<ProductOut> getProductsTwoLevelShard(Collection<String> ids) {
        callCount.incrementAndGet();
        log.debug("Fetching products with two-level cache + FIXED_SHARD, ids: {}", ids.size());
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return ids.stream()
                .map(productDatabase::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public int getCallCount() {
        return callCount.get();
    }

    public void resetCallCount() {
        callCount.set(0);
    }

    public int getEvictCallCount() {
        return evictCallCount.get();
    }

    public void resetEvictCallCount() {
        evictCallCount.set(0);
    }
}