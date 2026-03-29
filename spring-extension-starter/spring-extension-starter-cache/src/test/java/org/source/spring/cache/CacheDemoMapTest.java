package org.source.spring.cache;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.source.spring.cache.facade.TestProductFacade;
import org.source.spring.cache.facade.out.ProductOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Map缓存测试
 * 
 * <p>测试 @ConfigureCache 注解返回 Map 类型时的缓存行为。</p>
 * 
 * <h3>Map 缓存特点</h3>
 * <ul>
 *   <li>返回值类型为 Map&lt;K, V&gt;，K 自动作为缓存 key</li>
 *   <li>支持批量查询多个 key，结果以 Map 形式返回</li>
 *   <li>缓存命中后直接返回缓存的 Map 数据</li>
 *   <li>默认使用 PARTIAL_TRUST 策略，入参必须是可变集合</li>
 * </ul>
 * 
 * @see TestProductFacade#getProductMapByIds(java.util.Collection)
 */
@SpringBootTest
public class CacheDemoMapTest {

    @Autowired
    private TestProductFacade testProductFacade;

    @Autowired
    private CacheManager cacheManager;

    /**
     * 测试 Map 类型返回值的缓存功能
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>首次调用 getProductMapByIds([1,2])，期望返回包含2个产品的 Map</li>
     *   <li>二次调用相同参数，期望返回相同的 Map，且不调用业务方法</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次查询：Map.size()=2，包含 key "1" 和 "2"，callCount=1</li>
     *   <li>二次查询：返回相同的 Map 对象，callCount 仍为 1</li>
     * </ul>
     */
    @Test
    void shouldHandleMapCache() {
        clearAllCaches();
        testProductFacade.resetCallCount();

        List<String> ids1 = new ArrayList<>();
        ids1.add("1");
        ids1.add("2");
        Map<String, ProductOut> productMap1 = testProductFacade.getProductMapByIds(ids1);
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(productMap1).hasSize(2);
        assertThat(productMap1).containsKeys("1", "2");
        assertThat(firstCallCount).isEqualTo(1);

        List<String> ids2 = new ArrayList<>();
        ids2.add("1");
        ids2.add("2");
        Map<String, ProductOut> productMap2 = testProductFacade.getProductMapByIds(ids2);
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(productMap2).isEqualTo(productMap1);
        assertThat(secondCallCount).isEqualTo(1);
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