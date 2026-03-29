package org.source.spring.cache;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.source.spring.cache.facade.TestProductFacade;
import org.source.spring.cache.facade.out.ProductOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 部分缓存策略测试
 * 
 * <p>测试 TRUST 和 DISTRUST 两种部分缓存策略的行为差异。</p>
 * 
 * <h3>策略对比</h3>
 * <table border="1">
 *   <tr><th>策略</th><th>缓存命中时</th><th>缓存缺失时</th></tr>
 *   <tr><td>TRUST</td><td>直接返回缓存数据</td><td>不调用业务方法，只返回缓存中存在的数据</td></tr>
 *   <tr><td>DISTRUST</td><td>返回缓存数据</td><td>用缺失的 key 重新调用业务方法</td></tr>
 * </table>
 * 
 * @see TestProductFacade#getProductsByIdsWithTrustStrategy(java.util.Collection)
 * @see TestProductFacade#getProductsByIdsWithDistrustStrategy(java.util.Collection)
 */
@SpringBootTest
public class CacheDemoPartialStrategyTest {

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
     * 测试 TRUST 策略：信任缓存，不重新查询缺失数据
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>首次查询 ids=[1,2,3]，缓存全部数据，callCount=1</li>
     *   <li>二次查询 ids=[1,2,999]，其中1,2已缓存，999不存在</li>
     *   <li>TRUST 策略下，只返回缓存命中的数据，不调用业务方法</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次查询：products.size()=3, callCount=1</li>
     *   <li>二次查询：products.size()=2（只返回1,2），callCount=1（不重新查询）</li>
     *   <li>三次查询：products.size()=2, callCount=1</li>
     * </ul>
     */
    @Test
    void shouldUseTrustStrategyWhenPartialCacheMiss() {
        List<ProductOut> products1 = testProductFacade.getProductsByIdsWithTrustStrategy(Arrays.asList("1", "2", "3"));
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(products1).hasSize(3);
        assertThat(firstCallCount).isEqualTo(1);

        List<ProductOut> products2 = testProductFacade.getProductsByIdsWithTrustStrategy(Arrays.asList("1", "2", "999"));
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(products2).hasSize(2);
        assertThat(secondCallCount).isEqualTo(1);

        List<ProductOut> products3 = testProductFacade.getProductsByIdsWithTrustStrategy(Arrays.asList("1", "2", "999"));
        int thirdCallCount = testProductFacade.getCallCount();

        assertThat(products3).hasSize(2);
        assertThat(thirdCallCount).isEqualTo(1);
    }

    /**
     * 测试 DISTRUST 策略：对缓存缺失的数据重新查询
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>首次查询 ids=[1,2]，缓存数据，callCount=1</li>
     *   <li>二次查询 ids=[1,2,3]，其中3未缓存</li>
     *   <li>DISTRUST 策略下，因部分缺失触发完整重新查询</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次查询：products.size()=2, callCount=1</li>
     *   <li>二次查询：products.size()=3, callCount=2（触发重新查询）</li>
     *   <li>三次查询：products.size()=3, callCount=2（全部命中缓存）</li>
     * </ul>
     */
    @Test
    void shouldUseDistrustStrategyWhenAnyCacheMiss() {
        List<ProductOut> products1 = testProductFacade.getProductsByIdsWithDistrustStrategy(Arrays.asList("1", "2"));
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(products1).hasSize(2);
        assertThat(firstCallCount).isEqualTo(1);

        List<ProductOut> products2 = testProductFacade.getProductsByIdsWithDistrustStrategy(Arrays.asList("1", "2", "3"));
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(products2).hasSize(3);
        assertThat(secondCallCount).isEqualTo(2);

        List<ProductOut> products3 = testProductFacade.getProductsByIdsWithDistrustStrategy(Arrays.asList("1", "2", "3"));
        int thirdCallCount = testProductFacade.getCallCount();

        assertThat(products3).hasSize(3);
        assertThat(thirdCallCount).isEqualTo(2);
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