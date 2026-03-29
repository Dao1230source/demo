package org.source.spring.cache;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.source.spring.cache.facade.TestProductFacade;
import org.source.spring.cache.facade.out.ProductOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 二级缓存测试
 * 
 * <p>测试 JVM + Redis 二级缓存架构的缓存行为。</p>
 * 
 * <h3>二级缓存查询流程</h3>
 * <ol>
 *   <li>先查询 JVM 本地缓存（最快）</li>
 *   <li>JVM 缓存未命中 → 查询 Redis</li>
 *   <li>Redis 未命中 → 执行业务方法获取数据</li>
 *   <li>将数据同时存入 JVM 和 Redis</li>
 * </ol>
 * 
 * @see TestProductFacade#getHotProductById(String)
 */
@SpringBootTest
public class CacheDemoTwoLevelTest {

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
     * 测试二级缓存的基本功能
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>首次调用 getHotProductById("1")，期望从业务方法获取数据</li>
     *   <li>二次调用相同参数，期望从二级缓存（JVM或Redis）获取数据</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次查询：product 不为空, callCount=1</li>
     *   <li>二次查询：product 与首次相同, callCount=1（缓存命中）</li>
     * </ul>
     */
    @Test
    void shouldUseTwoLevelCache() {
        String productId = "1";

        ProductOut product1 = testProductFacade.getHotProductById(productId);
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(product1).isNotNull();
        assertThat(product1.getName()).isEqualTo("Laptop");
        assertThat(firstCallCount).isEqualTo(1);

        ProductOut product2 = testProductFacade.getHotProductById(productId);
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(product2).isEqualTo(product1);
        assertThat(secondCallCount).isEqualTo(1);
    }

    /**
     * 测试二级缓存处理多个产品
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>首次查询多个产品 id=1,2,3，每个都会调用业务方法</li>
     *   <li>二次查询相同产品，期望全部从缓存获取</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次查询：callCount=3（每个产品调用1次）</li>
     *   <li>二次查询：callCount=3（全部缓存命中，不再调用）</li>
     * </ul>
     */
    @Test
    void shouldCacheMultipleHotProducts() {
        ProductOut product1 = testProductFacade.getHotProductById("1");
        ProductOut product2 = testProductFacade.getHotProductById("2");
        ProductOut product3 = testProductFacade.getHotProductById("3");

        int callCount = testProductFacade.getCallCount();

        assertThat(product1).isNotNull();
        assertThat(product2).isNotNull();
        assertThat(product3).isNotNull();
        assertThat(callCount).isEqualTo(3);

        ProductOut cached1 = testProductFacade.getHotProductById("1");
        ProductOut cached2 = testProductFacade.getHotProductById("2");
        ProductOut cached3 = testProductFacade.getHotProductById("3");

        int secondCallCount = testProductFacade.getCallCount();

        assertThat(cached1).isEqualTo(product1);
        assertThat(cached2).isEqualTo(product2);
        assertThat(cached3).isEqualTo(product3);
        assertThat(secondCallCount).isEqualTo(3);
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