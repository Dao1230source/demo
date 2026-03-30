package org.source.spring.cache;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.source.spring.cache.facade.TestProductFacade;
import org.source.spring.cache.facade.out.ProductOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 分片缓存测试
 *
 * <p>测试 HSET 分片存储功能，验证 FIXED_SHARD 和 FIXED_SIZE 两种分片策略。</p>
 *
 * <h2>分片策略说明</h2>
 * <ul>
 *   <li><b>FIXED_SHARD</b>：按 hashCode 分片，shardValue 表示分片数量
 *       <p>shardIndex = hashCode(key) % shardValue</p>
 *   </li>
 *   <li><b>FIXED_SIZE</b>：按数值范围分片，shardValue 表示每个分片的数据量
 *       <p>shardIndex = key / shardValue + 1</p>
 *       <p>要求 key 必须是 Integer 或 Long 类型</p>
 *   </li>
 * </ul>
 */
@SpringBootTest
public class CacheDemoShardTest {

    @Autowired
    private TestProductFacade testProductFacade;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String FIXED_SHARD_CACHE = "productsFixedShardRedis";
    private static final String FIXED_SIZE_CACHE = "productsFixedSizeRedis";
    private static final String TWO_LEVEL_CACHE = "productsTwoLevelShard";

    @BeforeEach
    void setUp() {
        clearAllCaches();
        testProductFacade.resetCallCount();
    }

    @Nested
    @DisplayName("Redis FIXED_SHARD 分片测试")
    class FixedShardRedisTests {

        /**
         * 测试基本分片读写功能
         * 
         * <p><b>测试逻辑</b>：</p>
         * <ol>
         *   <li>查询50个产品ID（ID 1-50）</li>
         *   <li>验证首次查询触发数据库调用</li>
         *   <li>验证二次查询命中缓存，不触发数据库调用</li>
         *   <li>验证Redis中存在8个分片HSET key</li>
         *   <li>验证数据分散存储在多个分片中</li>
         * </ol>
         * 
         * <p><b>数据预期</b>：</p>
         * <ul>
         *   <li>返回50个产品</li>
         *   <li>首次callCount=1，二次callCount仍=1</li>
         *   <li>Redis存在 productsFixedShardRedis:shard:0 ~ :shard:7 共8个HSET</li>
         *   <li>每个分片约6-7条数据（50/8≈6.25）</li>
         * </ul>
         */
        @Test
        @DisplayName("基本分片读写 - 数据应分散到 8 个 HSET 分片")
        void shouldCacheWithFixedShardRedis() {
            List<String> ids = createStringIds(50);

            List<ProductOut> products1 = testProductFacade.getProductsFixedShardRedis(ids);
            int firstCallCount = testProductFacade.getCallCount();

            assertThat(products1).hasSize(50);
            assertThat(firstCallCount).isEqualTo(1);

            List<ProductOut> products2 = testProductFacade.getProductsFixedShardRedis(ids);
            int secondCallCount = testProductFacade.getCallCount();

            assertThat(products2).hasSize(50);
            assertThat(secondCallCount).isEqualTo(1);

            assertFixedShardRedisKeysExist(8);
            assertTotalFieldsInFixedShard(50);
        }

        /**
         * 测试部分缓存命中场景
         * 
         * <p><b>测试逻辑</b>：</p>
         * <ol>
         *   <li>首次查询30个ID（ID 1-30）</li>
         *   <li>二次查询50个ID（ID 1-50），其中20个是新ID</li>
         *   <li>验证PARTIAL_TRUST策略触发增量查询</li>
         * </ol>
         * 
         * <p><b>数据预期</b>：</p>
         * <ul>
         *   <li>首次查询返回30个产品，callCount=1</li>
         *   <li>二次查询返回50个产品，callCount=2（增量查询新ID）</li>
         *   <li>Redis分片中最终存储50条数据</li>
         * </ul>
         */
        @Test
        @DisplayName("部分缓存命中 - 新 key 应触发增量查询")
        void shouldHandlePartialCacheHitWithFixedShardRedis() {
            List<String> ids1 = createStringIds(30);

            List<ProductOut> products1 = testProductFacade.getProductsFixedShardRedis(ids1);
            int firstCallCount = testProductFacade.getCallCount();

            assertThat(products1).hasSize(30);
            assertThat(firstCallCount).isEqualTo(1);

            assertTotalFieldsInFixedShard(30);

            List<String> ids2 = createStringIds(50);

            List<ProductOut> products2 = testProductFacade.getProductsFixedShardRedis(ids2);
            int secondCallCount = testProductFacade.getCallCount();

            assertThat(products2).hasSize(50);
            assertThat(secondCallCount).isEqualTo(2);

            assertTotalFieldsInFixedShard(50);
        }

        /**
         * 测试跨分片查询
         * 
         * <p><b>测试逻辑</b>：</p>
         * <ol>
         *   <li>查询80个ID，验证数据分散在多个分片</li>
         *   <li>验证所有分片都有数据</li>
         * </ol>
         * 
         * <p><b>数据预期</b>：</p>
         * <ul>
         *   <li>返回80个产品</li>
         *   <li>callCount=1</li>
         *   <li>8个分片都有数据（每个约10条）</li>
         * </ul>
         */
        @Test
        @DisplayName("跨分片查询 - 请求分散在多个分片")
        void shouldQueryAcrossMultipleShards() {
            List<String> ids = createStringIds(80);

            List<ProductOut> products = testProductFacade.getProductsFixedShardRedis(ids);

            assertThat(products).hasSize(80);
            assertThat(testProductFacade.getCallCount()).isEqualTo(1);

            assertFixedShardRedisKeysExist(8);
            assertAllShardsHaveData(8);
        }

        private void assertFixedShardRedisKeysExist(int expectedShardCount) {
            Set<String> shardKeys = redisTemplate.keys("*" + FIXED_SHARD_CACHE + ":shard:*");
            assertThat(shardKeys).isNotNull();
            assertThat(shardKeys.size()).isEqualTo(expectedShardCount);
        }

        private void assertTotalFieldsInFixedShard(int expectedCount) {
            Set<String> shardKeys = redisTemplate.keys("*" + FIXED_SHARD_CACHE + ":shard:*");
            assertThat(shardKeys).isNotNull();
            int totalFields = 0;
            for (String shardKey : shardKeys) {
                Long size = redisTemplate.opsForHash().size(shardKey);
                totalFields += size.intValue();
            }
            assertThat(totalFields).isEqualTo(expectedCount);
        }

        private void assertAllShardsHaveData(int shardCount) {
            Set<String> shardKeys = redisTemplate.keys("*" + FIXED_SHARD_CACHE + ":shard:*");
            assertThat(shardKeys).isNotNull();
            assertThat(shardKeys.size()).isEqualTo(shardCount);
        }
    }

    @Nested
    @DisplayName("Redis FIXED_SIZE 分片测试")
    class FixedSizeRedisTests {

        /**
         * 测试按范围分片的基本读写
         * 
         * <p><b>测试逻辑</b>：</p>
         * <ol>
         *   <li>查询ID 1-50</li>
         *   <li>验证缓存命中</li>
         *   <li>验证Redis key结构正确</li>
         * </ol>
         * 
         * <p><b>分片计算</b>：shardIndex = (id / 50) + 1</p>
         * <ul>
         *   <li>ID 1-49: 0 + 1 = 1 → shard:1</li>
         *   <li>ID 50: 1 + 1 = 2 → shard:2</li>
         * </ul>
         * 
         * <p><b>数据预期</b>：</p>
         * <ul>
         *   <li>返回50个产品</li>
         *   <li>首次callCount=1，二次callCount仍=1</li>
         *   <li>shard:1 包含49个field（ID 1-49）</li>
         *   <li>shard:2 包含1个field（ID 50）</li>
         * </ul>
         */
        @Test
        @DisplayName("按范围分片 - ID 1-50 分布在 shard:1 和 shard:2")
        void shouldCacheWithFixedSizeRedis() {
            List<Long> ids = createLongIds(50);

            List<ProductOut> products1 = testProductFacade.getProductsFixedSizeRedis(ids);
            int firstCallCount = testProductFacade.getCallCount();

            assertThat(products1).hasSize(50);
            assertThat(firstCallCount).isEqualTo(1);

            List<ProductOut> products2 = testProductFacade.getProductsFixedSizeRedis(ids);
            int secondCallCount = testProductFacade.getCallCount();

            assertThat(products2).hasSize(50);
            assertThat(secondCallCount).isEqualTo(1);

            assertFixedSizeShardExists(1);
            assertFieldsInFixedSizeShard(0, 49);
            assertFixedSizeShardExists(1);
            assertFieldsInFixedSizeShard(1, 1);
        }

        /**
         * 测试跨分片边界查询
         * 
         * <p><b>测试逻辑</b>：</p>
         * <ol>
         *   <li>查询ID 1-100，跨越多个分片边界</li>
         *   <li>验证数据正确存储在多个分片中</li>
         * </ol>
         * 
         * <p><b>分片计算</b>：shardIndex = (id / 50) + 1</p>
         * <ul>
         *   <li>ID 1-49 → shard:1（49条）</li>
         *   <li>ID 50-99 → shard:2（50条）</li>
         *   <li>ID 100 → shard:3（1条）</li>
         * </ul>
         * 
         * <p><b>数据预期</b>：</p>
         * <ul>
         *   <li>返回100个产品</li>
         *   <li>callCount=1</li>
         *   <li>shard:1 包含49条</li>
         *   <li>shard:2 包含50条</li>
         *   <li>shard:3 包含1条</li>
         * </ul>
         */
        @Test
        @DisplayName("跨分片查询 - ID 1-100 分布在 shard:1、:2、:3")
        void shouldQueryAcrossShardBoundaries() {
            List<Long> ids = createLongIds(100);

            List<ProductOut> products = testProductFacade.getProductsFixedSizeRedis(ids);

            assertThat(products).hasSize(100);
            assertThat(testProductFacade.getCallCount()).isEqualTo(1);

            assertFixedSizeShardExists(0);
            assertFixedSizeShardExists(1);
            assertFixedSizeShardExists(2);
            assertFieldsInFixedSizeShard(0, 49);
            assertFieldsInFixedSizeShard(1, 50);
            assertFieldsInFixedSizeShard(2, 1);
        }

        /**
         * 测试部分命中场景
         * 
         * <p><b>测试逻辑</b>：</p>
         * <ol>
         *   <li>首次查询ID 1-30</li>
         *   <li>二次查询ID 1-60，包含新ID范围</li>
         *   <li>验证增量查询行为</li>
         * </ol>
         * 
         * <p><b>分片计算</b>：shardIndex = (id / 50) + 1</p>
         * <ul>
         *   <li>ID 1-30 全部落在 shard:1</li>
         *   <li>ID 31-49 落在 shard:1</li>
         *   <li>ID 50-60 落在 shard:2</li>
         * </ul>
         * 
         * <p><b>数据预期</b>：</p>
         * <ul>
         *   <li>首次返回30个产品，callCount=1</li>
         *   <li>二次返回60个产品，callCount=2</li>
         *   <li>shard:1 最终包含49个field（ID 1-49）</li>
         *   <li>shard:2 包含11个field（ID 50-60）</li>
         * </ul>
         */
        @Test
        @DisplayName("部分命中 - 新 ID 范围触发增量查询")
        void shouldHandlePartialCacheHitWithFixedSizeRedis() {
            List<Long> ids1 = LongStream.rangeClosed(1, 30).boxed().collect(Collectors.toList());

            List<ProductOut> products1 = testProductFacade.getProductsFixedSizeRedis(ids1);
            int firstCallCount = testProductFacade.getCallCount();

            assertThat(products1).hasSize(30);
            assertThat(firstCallCount).isEqualTo(1);

            List<Long> ids2 = LongStream.rangeClosed(1, 60).boxed().collect(Collectors.toList());

            List<ProductOut> products2 = testProductFacade.getProductsFixedSizeRedis(ids2);
            int secondCallCount = testProductFacade.getCallCount();

            assertThat(products2).hasSize(60);
            assertThat(secondCallCount).isEqualTo(2);

            assertFieldsInFixedSizeShard(0, 49);
            assertFieldsInFixedSizeShard(1, 11);
        }

        private void assertFixedSizeShardExists(int shardIndex) {
            Set<String> shardKeys = redisTemplate.keys("*" + FIXED_SIZE_CACHE + ":shard:" + shardIndex);
            assertThat(shardKeys).isNotNull();
            assertThat(shardKeys).isNotEmpty();
        }

        private void assertFieldsInFixedSizeShard(int shardIndex, int expectedCount) {
            Set<String> shardKeys = redisTemplate.keys("*" + FIXED_SIZE_CACHE + ":shard:" + shardIndex);
            assertThat(shardKeys).isNotNull();
            assertThat(shardKeys).isNotEmpty();
            String shardKey = shardKeys.iterator().next();
            Long size = redisTemplate.opsForHash().size(shardKey);
            assertThat(size).isNotNull();
            assertThat(size.intValue()).isEqualTo(expectedCount);
        }
    }

    @Nested
    @DisplayName("双级缓存 + 分片测试")
    class TwoLevelCacheShardTests {

        /**
         * 测试JVM缓存优先命中
         * 
         * <p><b>测试逻辑</b>：</p>
         * <ol>
         *   <li>首次查询30个ID，数据写入JVM和Redis</li>
         *   <li>二次查询相同ID，验证从JVM缓存获取</li>
         * </ol>
         * 
         * <p><b>数据预期</b>：</p>
         * <ul>
         *   <li>首次返回30个产品，callCount=1</li>
         *   <li>二次返回30个产品，callCount仍=1（JVM命中）</li>
         *   <li>Redis分片也存在数据</li>
         * </ul>
         */
        @Test
        @DisplayName("JVM 优先命中 - JVM 缓存命中则不查询 Redis")
        void shouldHitJvmCacheFirst() {
            List<String> ids = createStringIds(30);

            List<ProductOut> products1 = testProductFacade.getProductsTwoLevelShard(ids);
            int firstCallCount = testProductFacade.getCallCount();

            assertThat(products1).hasSize(30);
            assertThat(firstCallCount).isEqualTo(1);

            List<ProductOut> products2 = testProductFacade.getProductsTwoLevelShard(ids);
            int secondCallCount = testProductFacade.getCallCount();

            assertThat(products2).hasSize(30);
            assertThat(secondCallCount).isEqualTo(1);

            assertTwoLevelShardRedisHasData();
        }

        /**
         * 测试Redis回源场景
         * 
         * <p><b>测试逻辑</b>：</p>
         * <ol>
         *   <li>首次查询，数据写入JVM和Redis</li>
         *   <li>清除JVM缓存</li>
         *   <li>二次查询，验证从Redis分片获取数据</li>
         * </ol>
         * 
         * <p><b>数据预期</b>：</p>
         * <ul>
         *   <li>首次返回30个产品，callCount=1</li>
         *   <li>清除JVM后二次查询，callCount=2（触发数据库查询）</li>
         *   <li>注意：当前实现清除缓存后需要重新查询数据库</li>
         * </ul>
         */
        @Test
        @DisplayName("Redis 回源 - JVM 缓存失效后从 Redis 获取")
        void shouldFallbackToRedisWhenJvmCacheMiss() {
            List<String> ids = createStringIds(30);

            List<ProductOut> products1 = testProductFacade.getProductsTwoLevelShard(ids);
            assertThat(products1).hasSize(30);
            assertThat(testProductFacade.getCallCount()).isEqualTo(1);

            clearJvmCacheOnly();

            List<ProductOut> products2 = testProductFacade.getProductsTwoLevelShard(ids);
            assertThat(products2).hasSize(30);
            assertThat(testProductFacade.getCallCount()).isEqualTo(2);
        }

        private void assertTwoLevelShardRedisHasData() {
            Set<String> shardKeys = redisTemplate.keys("*" + TWO_LEVEL_CACHE + ":shard:*");
            assertThat(shardKeys).isNotNull();
            assertThat(shardKeys).isNotEmpty();
            int totalFields = 0;
            for (String shardKey : shardKeys) {
                Long size = redisTemplate.opsForHash().size(shardKey);
                totalFields += size.intValue();
            }
            assertThat(totalFields).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        /**
         * 测试空集合查询
         * 
         * <p><b>测试逻辑</b>：传入空集合，验证系统正常处理</p>
         * <p><b>数据预期</b>：返回空列表，不抛异常</p>
         */
        @Test
        @DisplayName("空集合查询")
        void shouldHandleEmptyCollection() {
            List<String> emptyIds = List.of();

            List<ProductOut> products = testProductFacade.getProductsFixedShardRedis(emptyIds);

            assertThat(products).isEmpty();
        }

        /**
         * 测试不存在的key查询
         * 
         * <p><b>测试逻辑</b>：查询数据库中不存在的ID（999, 1000, 1001）</p>
         * <p><b>数据预期</b>：返回空列表，不抛异常</p>
         */
        @Test
        @DisplayName("不存在的 key 查询")
        void shouldHandleNonExistentKeys() {
            List<String> ids = List.of("999", "1000", "1001");

            List<ProductOut> products = testProductFacade.getProductsFixedShardRedis(ids);

            assertThat(products).isEmpty();
        }

        /**
         * 测试混合存在/不存在的key
         * 
         * <p><b>测试逻辑</b>：查询混合存在和不存在的ID</p>
         * <p><b>数据预期</b>：仅返回存在的产品（ID 1和2），不包含999和1000</p>
         */
        @Test
        @DisplayName("混合存在/不存在的 key")
        void shouldHandleMixedKeys() {
            List<String> ids = new ArrayList<>();
            ids.add("1");
            ids.add("999");
            ids.add("2");
            ids.add("1000");

            List<ProductOut> products = testProductFacade.getProductsFixedShardRedis(ids);

            assertThat(products).hasSize(2);
            assertThat(products.stream().map(ProductOut::getId).collect(Collectors.toSet()))
                    .containsExactlyInAnyOrder("1", "2");
        }
    }

    private List<String> createStringIds(int count) {
        List<String> ids = new ArrayList<>();
        IntStream.rangeClosed(1, count).forEach(i -> ids.add(String.valueOf(i)));
        return ids;
    }

    private List<Long> createLongIds(int count) {
        return LongStream.rangeClosed(1, count).boxed().collect(Collectors.toList());
    }

    private void clearAllCaches() {
        if (CollectionUtils.isNotEmpty(cacheManager.getCacheNames())) {
            cacheManager.getCacheNames().forEach(name -> {
                var cache = cacheManager.getCache(name);
                if (cache != null) {
                    cache.clear();
                }
            });
        }
        Set<String> keys = redisTemplate.keys("*:shard:*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private void clearJvmCacheOnly() {
        var cache = cacheManager.getCache(CacheDemoShardTest.TWO_LEVEL_CACHE);
        if (cache != null) {
            cache.clear();
        }
    }
}