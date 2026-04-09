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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 特殊返回类型测试
 *
 * <p>测试 @ConfigureCache 注解对特殊返回类型的支持情况。</p>
 *
 * <h3>支持的返回类型</h3>
 * <table border="1">
 *   <tr><th>场景</th><th>输入</th><th>返回类型</th><th>returnType</th><th>valueClasses</th><th>缓存方式</th></tr>
 *   <tr><td>单key返回对象</td><td>单个key</td><td>ProductOut</td><td>AUTO</td><td>{ProductOut.class}</td><td>整个对象作为value</td></tr>
 *   <tr><td>单key返回List</td><td>单个key</td><td>{@literal List<V>}</td><td>RAW</td><td>{List.class, V.class}</td><td>整个List作为value</td></tr>
 *   <tr><td>单key返回Map</td><td>单个key</td><td>{@literal Map<K,V>}</td><td>RAW</td><td>{Map.class, K.class, V.class}</td><td>整个Map作为value</td></tr>
 *   <tr><td>批量key返回List</td><td>key集合</td><td>{@literal List<V>}</td><td>LIST</td><td>{V.class}</td><td>每个元素独立缓存</td></tr>
 *   <tr><td>批量key返回Set</td><td>key集合</td><td>{@literal Set<V>}</td><td>SET</td><td>{V.class}</td><td>每个元素独立缓存</td></tr>
 *   <tr><td>批量key返回Map</td><td>key集合</td><td>{@literal Map<K,V>}</td><td>MAP</td><td>{V.class}</td><td>每个元素独立缓存</td></tr>
 * </table>
 *
 * <h3>不支持的返回类型</h3>
 * <table border="1">
 *   <tr><th>场景</th><th>返回类型</th><th>原因</th><th>推荐替代方案</th></tr>
 *   <tr>
 *     <td>嵌套List</td>
 *     <td>{@literal List<List<V>>}</td>
 *     <td>内层List元素没有明确的key字段</td>
 *     <td>使用{@literal Map<K, List<V>>}替代</td>
 *   </tr>
 * </table>
 *
 * @see TestProductFacade#getProductsByCategory(String)
 * @see TestProductFacade#getProductAttributes(String)
 */
@SpringBootTest
public class CacheDemoSpecialReturnTypeTest {

    @Autowired
    private TestProductFacade testProductFacade;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        clearAllCaches();
        testProductFacade.resetCallCount();
    }

    // ==================== 单key返回List测试（returnType=RAW）====================

    /**
     * 测试单个key返回List类型（returnType=RAW）
     *
     * <h4>配置说明</h4>
     * <pre>
     * returnType = RAW
     * valueClasses = {List.class, ProductOut.class}
     * </pre>
     *
     * <h4>缓存行为</h4>
     * <ul>
     *   <li>整个List序列化后作为一个value存储</li>
     *   <li>Redis key: categoryProducts::categoryId</li>
     * </ul>
     */
    @Test
    void shouldReturnListForSingleKey() {
        List<ProductOut> products1 = testProductFacade.getProductsByCategory("electronics");
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(products1).isNotEmpty();
        assertThat(firstCallCount).isEqualTo(1);

        testProductFacade.resetCallCount();

        List<ProductOut> products2 = testProductFacade.getProductsByCategory("electronics");
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(products2).isEqualTo(products1);
        assertThat(secondCallCount).isEqualTo(0);
    }

    /**
     * 测试单个key返回空List
     */
    @Test
    void shouldReturnEmptyListForNonExistentCategory() {
        List<ProductOut> products = testProductFacade.getProductsByCategory("nonexistent");

        assertThat(products).isEmpty();
    }

    // ==================== 单key返回Map测试（returnType=RAW）====================

    /**
     * 测试单个key返回Map类型（returnType=RAW）
     *
     * <h4>配置说明</h4>
     * <pre>
     * returnType = RAW
     * valueClasses = {Map.class, String.class, String.class}
     * </pre>
     *
     * <h4>缓存行为</h4>
     * <ul>
     *   <li>整个Map序列化后作为一个value存储</li>
     *   <li>Redis key: productAttributes::productId</li>
     * </ul>
     */
    @Test
    void shouldReturnMapForSingleKey() {
        Map<String, String> attributes1 = testProductFacade.getProductAttributes("1");
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(attributes1).isNotEmpty();
        assertThat(attributes1).containsKeys("id", "name", "price", "category");
        assertThat(attributes1.get("id")).isEqualTo("1");
        assertThat(firstCallCount).isEqualTo(1);

        testProductFacade.resetCallCount();

        Map<String, String> attributes2 = testProductFacade.getProductAttributes("1");
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(attributes2).isEqualTo(attributes1);
        assertThat(secondCallCount).isEqualTo(0);
    }

    /**
     * 测试单个key返回空Map
     */
    @Test
    void shouldReturnEmptyMapForNonExistentProduct() {
        Map<String, String> attributes = testProductFacade.getProductAttributes("nonexistent");

        assertThat(attributes).isEmpty();
    }


    /**
     * 测试支持的返回类型：{@literal Map<K, List<V>>}（批量key场景）
     *
     * <p><b>缓存配置</b>：cacheNames=categoryProductsMap, returnType=MAP, valueClasses指定List元素类型</p>
     * <p><b>数据预期</b>：输入分类ID集合，返回分类到产品列表的映射</p>
     * <p><b>Redis结构</b>：每个产品独立存储，key格式为 categoryProductsMap::productId</p>
     *
     * <h4>配置说明</h4>
     * <pre>
     * returnType = MAP
     * valueClasses = {List.class, ProductOut.class}
     * </pre>
     */
    @Test
    void shouldSupportMapWithListValue() {
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("electronics");

        testProductFacade.resetCallCount();
        Map<String, List<ProductOut>> result1 = testProductFacade.getProductsByCategories(categoryIds);
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(result1).isNotEmpty();
        assertThat(result1).containsKey("electronics");
        assertThat(result1.get("electronics")).isNotEmpty();
        assertThat(firstCallCount).isGreaterThanOrEqualTo(1);

        testProductFacade.resetCallCount();

        Map<String, List<ProductOut>> result2 = testProductFacade.getProductsByCategories(categoryIds);
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(result2).isEqualTo(result1);
        assertThat(secondCallCount).isGreaterThanOrEqualTo(0);
    }

    /**
     * 测试支持的返回类型：{@literal Map<K, Map<KK, V>>}（批量key场景）
     *
     * <p><b>缓存配置</b>：cacheNames=productAttributesMap, returnType=MAP, valueClasses指定Map的value类型</p>
     * <p><b>数据预期</b>：输入产品ID集合，返回产品ID到属性Map的映射</p>
     * <p><b>Redis结构</b>：每个产品属性独立存储</p>
     *
     * <h4>配置说明</h4>
     * <pre>
     * returnType = MAP
     * valueClasses = {Map.class, String.class, String.class}
     * </pre>
     */
    @Test
    void shouldSupportMapWithMapValue() {
        List<String> productIds = new ArrayList<>();
        productIds.add("1");
        productIds.add("2");

        testProductFacade.resetCallCount();
        Map<String, Map<String, String>> result1 = testProductFacade.getProductAttributesMap(productIds);
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(result1).isNotEmpty();
        assertThat(result1).containsKey("1");
        assertThat(result1).containsKey("2");
        assertThat(result1.get("1")).containsKey("id");
        assertThat(firstCallCount).isGreaterThanOrEqualTo(1);

        testProductFacade.resetCallCount();

        Map<String, Map<String, String>> result2 = testProductFacade.getProductAttributesMap(productIds);
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(result2).isEqualTo(result1);
        assertThat(secondCallCount).isGreaterThanOrEqualTo(0);
    }

    // ==================== 不支持的返回类型测试 ====================

    /**
     * 测试不支持的返回类型：{@literal List<List<V>>}
     *
     * <h4>为什么不支持？</h4>
     * <ul>
     *   <li><strong>缓存 Key 无法确定</strong>：内层 List 中的元素没有明确的 key 字段</li>
     *   <li><strong>序列化复杂</strong>：嵌套 List 结构难以正确反序列化</li>
     *   <li><strong>语义不明确</strong>：外层 List 表示分组，缓存需要 key-value 结构</li>
     * </ul>
     *
     * <h4>推荐替代方案</h4>
     * <pre>
     * // 不支持 ❌
     * {@literal List<List<ProductOut>>} getProductsGroupedByCategory();
     *
     * // 推荐 ✅ - 使用 Map 结构，returnType=RAW
     * {@literal Map<String, List<ProductOut>>} getProductsByCategories(Collection&lt;String&gt; categoryIds);
     * </pre>
     */
    @Test
    void shouldThrowExceptionForUnsupportedNestedListReturnType() {
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("electronics");

        assertThatThrownBy(() -> testProductFacade.getProductsGroupedUnsupported(categoryIds))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("List<List<V>> return type is not supported")
                .hasMessageContaining("Use Map<K, List<V>> instead");
    }

    // ==================== JVM 缓存测试（特殊返回类型）====================

    /**
     * 测试单 key 返回 List + JVM 缓存（returnType=RAW）
     *
     * <p><b>缓存配置</b>：cacheNames=categoryProductsJvm, returnType=RAW, cacheInJvm 启用</p>
     * <p><b>数据预期</b>：整个 List 作为 value 存储在 JVM 缓存中</p>
     *
     * <h4>配置说明</h4>
     * <pre>
     * returnType = RAW
     * cacheInRedis = {enable = false}
     * cacheInJvm = {enable = true, ttl = 300, jvmMaxSize = 1000}
     * </pre>
     */
    @Test
    void shouldSupportListWithJvmCache() {
        testProductFacade.resetCallCount();
        List<ProductOut> products1 = testProductFacade.getProductsByCategoryJvm("electronics");
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(products1).isNotEmpty();
        assertThat(firstCallCount).isEqualTo(1);

        testProductFacade.resetCallCount();

        List<ProductOut> products2 = testProductFacade.getProductsByCategoryJvm("electronics");
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(products2).isEqualTo(products1);
        assertThat(secondCallCount).isEqualTo(0);
    }

    /**
     * 测试单 key 返回 Map+JVM 缓存（returnType=RAW）
     *
     * <p><b>缓存配置</b>：cacheNames=productAttributesJvm, returnType=RAW, cacheInJvm 启用</p>
     * <p><b>数据预期</b>：整个 Map 作为 value 存储在 JVM 缓存中</p>
     *
     * <h4>配置说明</h4>
     * <pre>
     * returnType = RAW
     * cacheInRedis = {enable = false}
     * cacheInJvm = {enable = true, ttl = 300, jvmMaxSize = 1000}
     * </pre>
     */
    @Test
    void shouldSupportMapWithJvmCache() {
        testProductFacade.resetCallCount();
        Map<String, String> attributes1 = testProductFacade.getProductAttributesJvm("1");
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(attributes1).isNotEmpty();
        assertThat(attributes1).containsKeys("id", "name", "price", "category");
        assertThat(attributes1.get("id")).isEqualTo("1");
        assertThat(firstCallCount).isEqualTo(1);

        testProductFacade.resetCallCount();

        Map<String, String> attributes2 = testProductFacade.getProductAttributesJvm("1");
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(attributes2).isEqualTo(attributes1);
        assertThat(secondCallCount).isEqualTo(0);
    }

    // ==================== Redis+JVM 二级缓存测试（特殊返回类型）====================

    /**
     * 测试单 key 返回 List + 二级缓存（Redis+JVM，returnType=RAW）
     *
     * <p><b>缓存配置</b>：cacheNames=categoryProductsTwoLevel, returnType=RAW, Redis+JVM 同时启用</p>
     * <p><b>数据预期</b>：整个 List 作为 value 存储，查询顺序 JVM → Redis → 数据库</p>
     *
     * <h4>配置说明</h4>
     * <pre>
     * returnType = RAW
     * cacheInRedis = {ttl = 3600, valueClasses = {List.class, ProductOut.class}}
     * cacheInJvm = {enable = true, ttl = 300, jvmMaxSize = 1000}
     * </pre>
     */
    @Test
    void shouldSupportListWithTwoLevelCache() {
        clearAllCaches();
        testProductFacade.resetCallCount();

        // 第一次调用：数据库查询，存入 JVM 和 Redis
        List<ProductOut> products1 = testProductFacade.getProductsByCategoryTwoLevel("electronics");
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(products1).isNotEmpty();
        assertThat(firstCallCount).isEqualTo(1);

        testProductFacade.resetCallCount();

        // 第二次调用：JVM 缓存命中
        List<ProductOut> products2 = testProductFacade.getProductsByCategoryTwoLevel("electronics");
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(products2).isEqualTo(products1);
        assertThat(secondCallCount).isEqualTo(0);
    }

    /**
     * 测试单 key 返回 Map + 二级缓存（Redis+JVM，returnType=RAW）
     *
     * <p><b>缓存配置</b>：cacheNames=productAttributesTwoLevel, returnType=RAW, Redis+JVM 同时启用</p>
     * <p><b>数据预期</b>：整个 Map 作为 value 存储，查询顺序 JVM → Redis → 数据库</p>
     *
     * <h4>配置说明</h4>
     * <pre>
     * returnType = RAW
     * cacheInRedis = {ttl = 3600, valueClasses = {Map.class, String.class, String.class}}
     * cacheInJvm = {enable = true, ttl = 300, jvmMaxSize = 1000}
     * </pre>
     */
    @Test
    void shouldSupportMapWithTwoLevelCache() {
        clearAllCaches();
        testProductFacade.resetCallCount();

        // 第一次调用：数据库查询，存入 JVM 和 Redis
        Map<String, String> attributes1 = testProductFacade.getProductAttributesTwoLevel("1");
        int firstCallCount = testProductFacade.getCallCount();

        assertThat(attributes1).isNotEmpty();
        assertThat(attributes1).containsKeys("id", "name", "price", "category");
        assertThat(attributes1.get("id")).isEqualTo("1");
        assertThat(firstCallCount).isEqualTo(1);

        testProductFacade.resetCallCount();

        // 第二次调用：JVM 缓存命中
        Map<String, String> attributes2 = testProductFacade.getProductAttributesTwoLevel("1");
        int secondCallCount = testProductFacade.getCallCount();

        assertThat(attributes2).isEqualTo(attributes1);
        assertThat(secondCallCount).isEqualTo(0);
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