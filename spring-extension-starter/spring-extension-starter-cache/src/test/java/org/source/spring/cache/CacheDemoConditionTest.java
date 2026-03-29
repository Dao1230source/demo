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
 * 条件缓存测试
 * 
 * <p>测试 @ConfigureCache 注解的 condition 条件表达式功能。</p>
 * 
 * <h3>条件表达式说明</h3>
 * <p>condition 属性使用 SpEL 表达式，当表达式返回 true 时才使用缓存。</p>
 * 
 * <h4>示例表达式</h4>
 * <ul>
 *   <li>{@code #id != null} - 参数不为空时缓存</li>
 *   <li>{@code #id.startsWith('1')} - id 以 "1" 开头时缓存</li>
 *   <li>{@code #result != null} - 返回值不为空时缓存</li>
 * </ul>
 * 
 * @see TestProductFacade#getProductWithCondition(String)
 */
@SpringBootTest
public class CacheDemoConditionTest {

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
     * 测试条件满足时使用缓存
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>首次调用 getProductWithCondition("1")，id 以 "1" 开头，满足条件</li>
     *   <li>二次调用相同参数，期望从缓存获取，不调用业务方法</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次查询：product 不为空, callCount=1</li>
     *   <li>二次查询：product 相同, callCount=1（缓存命中）</li>
     * </ul>
     */
    @Test
    void shouldCacheWhenConditionMatches() {
        ProductOut product1 = testProductFacade.getProductWithCondition("1");
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(product1).isNotNull();
        assertThat(firstCallCount).isEqualTo(1);

        ProductOut product2 = testProductFacade.getProductWithCondition("1");
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(product2).isEqualTo(product1);
        assertThat(secondCallCount).isEqualTo(1);
    }

    /**
     * 测试条件不满足时不使用缓存（id 以 "2" 开头）
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>首次调用 getProductWithCondition("2")，id 不以 "1" 开头，不满足条件</li>
     *   <li>二次调用相同参数，期望不使用缓存，重新调用业务方法</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次查询：product 不为空, callCount=1</li>
     *   <li>二次查询：product 不为空, callCount=2（不使用缓存）</li>
     * </ul>
     */
    @Test
    void shouldNotCacheWhenConditionNotMatches() {
        ProductOut product1 = testProductFacade.getProductWithCondition("2");
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(product1).isNotNull();
        assertThat(firstCallCount).isEqualTo(1);

        ProductOut product2 = testProductFacade.getProductWithCondition("2");
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(product2).isNotNull();
        assertThat(secondCallCount).isEqualTo(2);
    }

    /**
     * 测试条件不满足时不使用缓存（id 以 "3" 开头）
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>首次调用 getProductWithCondition("3")，id 不以 "1" 开头，不满足条件</li>
     *   <li>二次调用相同参数，期望不使用缓存</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次查询：product 不为空, callCount=1</li>
     *   <li>二次查询：product 不为空, callCount=2（不使用缓存）</li>
     * </ul>
     */
    @Test
    void shouldNotCacheWhenIdStartsWithOtherDigit() {
        ProductOut product1 = testProductFacade.getProductWithCondition("3");
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(product1).isNotNull();
        assertThat(firstCallCount).isEqualTo(1);

        ProductOut product2 = testProductFacade.getProductWithCondition("3");
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(product2).isNotNull();
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