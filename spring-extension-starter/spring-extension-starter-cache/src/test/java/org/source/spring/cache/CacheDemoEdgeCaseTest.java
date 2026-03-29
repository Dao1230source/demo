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
 * 边界情况测试
 * 
 * <p>测试缓存处理各种边界情况和异常输入的能力。</p>
 * 
 * <h3>测试场景</h3>
 * <ul>
 *   <li>查询不存在数据返回 null</li>
 *   <li>传入空字符串或 null 参数</li>
 *   <li>传入空集合</li>
 *   <li>传入不存在的 ID 集合</li>
 *   <li>传入混合存在和不存在的 ID</li>
 * </ul>
 * 
 * @see TestProductFacade#getProductOrNull(String)
 * @see TestProductFacade#getProductsByIds(java.util.Collection)
 */
@SpringBootTest
public class CacheDemoEdgeCaseTest {

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
     * 测试查询不存在的数据返回 null
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>调用 getProductOrNull("999")，查询不存在的产品ID</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>返回 null</li>
     *   <li>callCount=1（业务方法被调用1次）</li>
     * </ul>
     */
    @Test
    void shouldHandleNullResult() {
        ProductOut product = testProductFacade.getProductOrNull("999");

        int callCount = testProductFacade.getCallCount();

        assertThat(product).isNull();
        assertThat(callCount).isEqualTo(1);
    }

    /**
     * 测试传入空字符串参数
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>调用 getProductOrNull("")，传入空字符串</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>返回 null</li>
     *   <li>callCount=1</li>
     * </ul>
     */
    @Test
    void shouldHandleEmptyId() {
        ProductOut product = testProductFacade.getProductOrNull("");

        int callCount = testProductFacade.getCallCount();

        assertThat(product).isNull();
        assertThat(callCount).isEqualTo(1);
    }

    /**
     * 测试传入 null 参数
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>调用 getProductOrNull(null)，传入 null</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>返回 null</li>
     *   <li>callCount=1</li>
     * </ul>
     */
    @Test
    void shouldHandleNullId() {
        ProductOut product = testProductFacade.getProductOrNull(null);

        int callCount = testProductFacade.getCallCount();

        assertThat(product).isNull();
        assertThat(callCount).isEqualTo(1);
    }

    /**
     * 测试传入空集合
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>调用 getProductsByIds(空ArrayList)</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>返回空 List</li>
     *   <li>callCount=1</li>
     * </ul>
     */
    @Test
    void shouldHandleEmptyCollectionInput() {
        List<String> ids = new ArrayList<>();
        List<ProductOut> products = testProductFacade.getProductsByIds(ids);

        int callCount = testProductFacade.getCallCount();

        assertThat(products).isEmpty();
        assertThat(callCount).isEqualTo(1);
    }

    /**
     * 测试传入全部不存在的 ID 集合
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>调用 getProductsByIds([999, 888, 777])，全部是不存在的ID</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>返回空 List</li>
     *   <li>callCount=1</li>
     * </ul>
     */
    @Test
    void shouldHandleNonExistentIdsInBatch() {
        List<String> ids = new ArrayList<>();
        ids.add("999");
        ids.add("888");
        ids.add("777");
        List<ProductOut> products = testProductFacade.getProductsByIds(ids);

        int callCount = testProductFacade.getCallCount();

        assertThat(products).isEmpty();
        assertThat(callCount).isEqualTo(1);
    }

    /**
     * 测试传入混合存在和不存在的 ID
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>调用 getProductsByIds([1, 999, 2, 888])，混合存在和不存在的ID</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>返回 List.size()=2，只包含 id=1 和 id=2 的产品</li>
     *   <li>callCount=1</li>
     * </ul>
     */
    @Test
    void shouldHandleMixedExistingAndNonExistingIds() {
        List<String> ids = new ArrayList<>();
        ids.add("1");
        ids.add("999");
        ids.add("2");
        ids.add("888");
        List<ProductOut> products = testProductFacade.getProductsByIds(ids);

        int callCount = testProductFacade.getCallCount();

        assertThat(products).hasSize(2);
        assertThat(products.stream().map(ProductOut::getId)).containsExactlyInAnyOrder("1", "2");
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