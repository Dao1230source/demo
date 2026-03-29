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
 * 缓存失效测试
 * 
 * <p>测试 @CacheEvict 注解的缓存失效功能。</p>
 * 
 * <h3>@CacheEvict 功能</h3>
 * <ul>
 *   <li>删除指定 key 的缓存项：{@code @CacheEvict(key = "#id")}</li>
 *   <li>删除所有缓存项：{@code @CacheEvict(allEntries = true)}</li>
 * </ul>
 * 
 * @see TestProductFacade#evictProduct(String)
 * @see TestProductFacade#evictAllProducts()
 */
@SpringBootTest
public class CacheDemoEvictTest {

    @Autowired
    private TestProductFacade testProductFacade;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        clearAllCaches();
        testProductFacade.resetCallCount();
        testProductFacade.resetEvictCallCount();
    }

    /**
     * 测试删除单个缓存项
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>首次查询 id=1，缓存数据，callCount=1</li>
     *   <li>二次查询 id=1，缓存命中，callCount=1</li>
     *   <li>调用 evictProduct("1") 删除缓存</li>
     *   <li>三次查询 id=1，缓存未命中，重新调用业务方法</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次和二次查询后 callCount=1</li>
     *   <li>evictProduct 调用后 evictCallCount=1</li>
     *   <li>三次查询后 callCount=2（重新调用业务方法）</li>
     * </ul>
     */
    @Test
    void shouldEvictSingleCache() {
        ProductOut product1 = testProductFacade.getProductById("1");
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(product1).isNotNull();
        assertThat(firstCallCount).isEqualTo(1);

        ProductOut cachedProduct = testProductFacade.getProductById("1");
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(cachedProduct).isEqualTo(product1);
        assertThat(secondCallCount).isEqualTo(1);

        testProductFacade.evictProduct("1");
        int evictCount = testProductFacade.getEvictCallCount();

        assertThat(evictCount).isEqualTo(1);

        ProductOut productAfterEvict = testProductFacade.getProductById("1");
        int thirdCallCount = testProductFacade.getCallCount();

        assertThat(productAfterEvict).isNotNull();
        assertThat(thirdCallCount).isEqualTo(2);
    }

    /**
     * 测试删除所有缓存项
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>查询 id=1 和 id=2，缓存数据，callCount=2</li>
     *   <li>再次查询 id=1 和 id=2，缓存命中，callCount=2</li>
     *   <li>调用 evictAllProducts() 清空所有缓存</li>
     *   <li>再次查询 id=1 和 id=2，缓存未命中，重新调用业务方法</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>清空前 callCount=2</li>
     *   <li>清空后 callCount=4（重新查询了2次）</li>
     * </ul>
     */
    @Test
    void shouldEvictAllCache() {
        testProductFacade.getProductById("1");
        testProductFacade.getProductById("2");
        int initialCallCount = testProductFacade.getCallCount();

        assertThat(initialCallCount).isEqualTo(2);

        testProductFacade.getProductById("1");
        testProductFacade.getProductById("2");

        int cachedCallCount = testProductFacade.getCallCount();

        assertThat(cachedCallCount).isEqualTo(2);

        testProductFacade.evictAllProducts();

        testProductFacade.getProductById("1");
        testProductFacade.getProductById("2");

        int afterEvictCallCount = testProductFacade.getCallCount();

        assertThat(afterEvictCallCount).isEqualTo(4);
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