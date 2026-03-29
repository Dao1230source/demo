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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 返回类型测试
 * 
 * <p>测试 @ConfigureCache 注解支持的不同返回类型，包括 Set、List、Map 等。</p>
 * 
 * <h3>支持的返回类型</h3>
 * <ul>
 *   <li>LIST - 返回 List&lt;E&gt; 类型</li>
 *   <li>SET - 返回 Set&lt;E&gt; 类型（自动去重）</li>
 *   <li>MAP - 返回 Map&lt;K, V&gt; 类型</li>
 *   <li>AUTO - 自动判断（默认）</li>
 * </ul>
 * 
 * <h3>注意事项</h3>
 * <p>默认使用 PARTIAL_TRUST 策略，入参必须是可变集合（如 ArrayList）</p>
 * 
 * @see TestProductFacade#getProductsByIdsAsSet(java.util.Collection)
 */
@SpringBootTest
public class CacheDemoReturnTypeTest {

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
     * 测试 Set 类型返回值的缓存功能
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>首次调用 getProductsByIdsAsSet([1,2,3])，期望返回包含3个产品的 Set</li>
     *   <li>二次调用相同参数，期望返回相同的 Set，且不调用业务方法</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次查询：Set.size()=3, callCount=1</li>
     *   <li>二次查询：Set.size()=3, callCount=1（缓存命中）</li>
     * </ul>
     */
    @Test
    void shouldReturnSetType() {
        List<String> ids1 = new ArrayList<>();
        ids1.add("1");
        ids1.add("2");
        ids1.add("3");
        Set<ProductOut> products1 = testProductFacade.getProductsByIdsAsSet(ids1);
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(products1).hasSize(3);
        assertThat(firstCallCount).isEqualTo(1);

        List<String> ids2 = new ArrayList<>();
        ids2.add("1");
        ids2.add("2");
        ids2.add("3");
        Set<ProductOut> products2 = testProductFacade.getProductsByIdsAsSet(ids2);
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(products2).hasSize(3);
        assertThat(secondCallCount).isEqualTo(1);
    }

    /**
     * 测试 Set 类型缓存处理空结果
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>调用 getProductsByIdsAsSet([999,888])，查询不存在的产品ID</li>
     *   <li>期望返回空 Set，且调用业务方法1次</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>Set.size()=0（空集合）</li>
     *   <li>callCount=1（业务方法被调用1次）</li>
     * </ul>
     */
    @Test
    void shouldHandleEmptySet() {
        List<String> ids = new ArrayList<>();
        ids.add("999");
        ids.add("888");
        Set<ProductOut> products = testProductFacade.getProductsByIdsAsSet(ids);
        int callCount = testProductFacade.getCallCount();

        assertThat(products).isEmpty();
        assertThat(callCount).isEqualTo(1);
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