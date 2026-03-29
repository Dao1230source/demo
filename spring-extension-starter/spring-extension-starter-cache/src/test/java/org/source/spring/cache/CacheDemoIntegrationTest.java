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
 * 缓存集成测试
 * 
 * <p>测试 @ConfigureCache 注解的基础功能，验证单条数据的缓存行为。</p>
 * 
 * <h3>测试场景</h3>
 * <ul>
 *   <li>单条数据首次查询：调用业务方法并存入缓存</li>
 *   <li>单条数据二次查询：从缓存获取，不调用业务方法</li>
 *   <li>缓存数据验证：验证缓存中存储的数据与返回数据一致</li>
 * </ul>
 * 
 * @see TestProductFacade#getProductById(String)
 */
@SpringBootTest
public class CacheDemoIntegrationTest {

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
     * 测试单条产品缓存功能
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>首次调用 getProductById("1")，期望返回产品数据，调用计数为 1</li>
     *   <li>二次调用 getProductById("1")，期望返回相同数据，调用计数仍为 1（从缓存获取）</li>
     *   <li>验证缓存中存储了正确的产品数据</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次调用后 callCount = 1</li>
     *   <li>二次调用后 callCount 仍为 1，表示缓存命中</li>
     *   <li>缓存中 key="1" 对应的值为首次查询的产品对象</li>
     * </ul>
     */
    @Test
    void shouldCacheSingleProduct() {
        String productId = "1";

        ProductOut product1 = testProductFacade.getProductById(productId);
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(product1).isNotNull();
        assertThat(product1.getName()).isEqualTo("Laptop");
        assertThat(firstCallCount).isEqualTo(1);

        ProductOut product2 = testProductFacade.getProductById(productId);
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(product2).isEqualTo(product1);
        assertThat(secondCallCount).isEqualTo(1);

        verifyCacheContainsValue("product", productId, product1);
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

    private void verifyCacheContainsValue(String cacheName, String key, ProductOut expectedValue) {
        var cache = cacheManager.getCache(cacheName);
        assertThat(cache).isNotNull();
        var cachedValue = cache.get(key, ProductOut.class);
        assertThat(cachedValue).isEqualTo(expectedValue);
    }
}