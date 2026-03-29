package org.source.spring.cache;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.source.spring.cache.facade.TestProductFacade;
import org.source.spring.cache.facade.out.ProductOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JVM缓存专用测试
 * 
 * <p>测试仅使用 JVM 本地缓存（禁用 Redis）的缓存行为。</p>
 * 
 * <h3>JVM 缓存配置</h3>
 * <ul>
 *   <li>cacheInRedis.enable = false：禁用 Redis 缓存</li>
 *   <li>cacheInJvm.enable = true：启用 JVM 本地缓存</li>
 *   <li>cacheInJvm.ttl = 60：JVM 缓存过期时间 60 秒</li>
 * </ul>
 * 
 * <h3>适用场景</h3>
 * <p>适用于数据量小、更新频率低、无需分布式同步的场景。</p>
 * 
 * <h3>注意事项</h3>
 * <p>默认使用 PARTIAL_TRUST 策略，入参必须是可变集合（如 ArrayList）</p>
 * 
 * @see TestProductFacade#getProductsJvmOnly(java.util.Collection)
 */
@SpringBootTest
public class CacheDemoJvmOnlyTest {

    @Autowired
    private TestProductFacade testProductFacade;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        clearAllCaches();
        testProductFacade.resetCallCount();
    }

    /**
     * 测试 JVM 缓存的基本功能
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>首次调用 getProductsJvmOnly([1,2,3])，期望从业务方法获取数据并存入 JVM 缓存</li>
     *   <li>二次调用相同参数，期望从 JVM 缓存获取数据</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次查询：products.size()=3, callCount=1</li>
     *   <li>二次查询：products.size()=3, callCount=1（JVM 缓存命中）</li>
     * </ul>
     */
    @Test
    void shouldCacheInJvmOnly() {
        List<String> ids1 = new ArrayList<>();
        ids1.add("1");
        ids1.add("2");
        ids1.add("3");
        List<ProductOut> products1 = testProductFacade.getProductsJvmOnly(ids1);
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(products1).hasSize(3);
        assertThat(firstCallCount).isEqualTo(1);

        List<String> ids2 = new ArrayList<>();
        ids2.add("1");
        ids2.add("2");
        ids2.add("3");
        List<ProductOut> products2 = testProductFacade.getProductsJvmOnly(ids2);
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(products2).hasSize(3);
        assertThat(secondCallCount).isEqualTo(1);
    }

    /**
     * 测试 JVM 缓存处理部分结果
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>首次查询 ids=[1,2]，缓存数据，callCount=1</li>
     *   <li>二次查询 ids=[1,2,3]，其中1,2已缓存，3未缓存</li>
     *   <li>期望触发对 id=3 的查询，callCount=2</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次查询：products.size()=2, callCount=1</li>
     *   <li>二次查询：products.size()=3, callCount=2（触发部分查询）</li>
     * </ul>
     */
    @Test
    void shouldCachePartialResultsInJvm() {
        List<String> ids1 = new ArrayList<>();
        ids1.add("1");
        ids1.add("2");
        List<ProductOut> products1 = testProductFacade.getProductsJvmOnly(ids1);
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(products1).hasSize(2);
        assertThat(firstCallCount).isEqualTo(1);

        List<String> ids2 = new ArrayList<>();
        ids2.add("1");
        ids2.add("2");
        ids2.add("3");
        List<ProductOut> products2 = testProductFacade.getProductsJvmOnly(ids2);
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(products2).hasSize(3);
        assertThat(secondCallCount).isEqualTo(2);
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
    }
}