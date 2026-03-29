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
 * 批量缓存测试
 * 
 * <p>测试 @ConfigureCache 注解的批量缓存功能，使用 PARTIAL_TRUST 部分缓存策略。</p>
 * 
 * <h3>PARTIAL_TRUST 策略说明</h3>
 * <ul>
 *   <li>缓存命中的数据直接返回</li>
 *   <li>对缺失的 key 调用业务方法重新查询</li>
 *   <li>方法入参必须是可变 Collection（如 ArrayList）</li>
 * </ul>
 * 
 * @see TestProductFacade#getProductsByIds(java.util.Collection)
 * @see org.source.spring.cache.strategy.PartialCacheStrategyEnum#PARTIAL_TRUST
 */
@SpringBootTest
public class CacheDemoBatchTest {

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
     * 测试批量缓存与 PARTIAL_TRUST 策略
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>首次批量查询 ids=[1,2,3]，期望返回3条数据，调用计数为1</li>
     *   <li>二次批量查询 ids=[1,2,4]，其中1,2已缓存，4未缓存</li>
     *   <li>期望返回3条数据（1,2从缓存获取，4重新查询），调用计数为2</li>
     *   <li>三次批量查询 ids=[1,2,4]，全部命中缓存，调用计数仍为2</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次查询：products.size()=3, callCount=1</li>
     *   <li>二次查询：products.size()=3, callCount=2（触发对id=4的查询）</li>
     *   <li>三次查询：products.size()=3, callCount=2（全部缓存命中）</li>
     * </ul>
     */
    @Test
    void shouldHandleBatchCacheWithPartialTrust() {
        List<String> ids1 = new ArrayList<>();
        ids1.add("1");
        ids1.add("2");
        ids1.add("3");
        List<ProductOut> products1 = testProductFacade.getProductsByIds(ids1);
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(products1).hasSize(3);
        assertThat(firstCallCount).isEqualTo(1);

        List<String> ids2 = new ArrayList<>();
        ids2.add("1");
        ids2.add("2");
        ids2.add("4");
        List<ProductOut> products2 = testProductFacade.getProductsByIds(ids2);
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(products2).hasSize(3);
        assertThat(secondCallCount).isEqualTo(2);

        List<String> ids3 = new ArrayList<>();
        ids3.add("1");
        ids3.add("2");
        ids3.add("4");
        List<ProductOut> products3 = testProductFacade.getProductsByIds(ids3);
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