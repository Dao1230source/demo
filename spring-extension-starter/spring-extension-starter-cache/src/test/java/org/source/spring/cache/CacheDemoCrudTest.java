package org.source.spring.cache;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.source.spring.cache.domain.entity.OrderEntity;
import org.source.spring.cache.domain.repository.OrderRepository;
import org.source.spring.cache.domain.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 订单 CRUD 缓存测试
 * 
 * <p>测试新增、更新、批量新增、批量更新、删除、批量删除操作与缓存的集成行为。</p>
 * 
 * <h3>测试场景</h3>
 * <ul>
 *   <li>新增订单后从数据库查询</li>
 *   <li>批量新增订单后批量查询</li>
 *   <li>更新订单后缓存失效</li>
 *   <li>批量更新订单后缓存失效</li>
 *   <li>删除订单后缓存失效</li>
 *   <li>批量删除订单后缓存失效</li>
 * </ul>
 */
@SpringBootTest
public class CacheDemoCrudTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        clearAllCaches();
        orderService.resetCallCount();
        orderRepository.deleteAll();
    }

    /**
     * 测试新增订单后从数据库查询
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>创建新订单 id="new-1"，productName="New Laptop"</li>
     *   <li>调用 createOrder 方法保存到数据库</li>
     *   <li>调用 getOrderById 方法查询订单，触发缓存</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>创建成功：返回的订单 id 和 productName 正确</li>
     *   <li>查询成功：从数据库获取数据，callCount=1</li>
     *   <li>数据一致性：查询结果与创建数据一致</li>
     * </ul>
     */
    @Test
    void shouldCreateOrderAndQueryFromDatabase() {
        OrderEntity newOrder = OrderEntity.builder()
                .id("new-1")
                .productName("New Laptop")
                .quantity(1)
                .price(1299.99)
                .build();

        OrderEntity created = orderService.createOrder(newOrder);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo("new-1");
        assertThat(created.getProductName()).isEqualTo("New Laptop");

        OrderEntity queried = orderService.getOrderById("new-1");
        int callCount = orderService.getCallCount();

        assertThat(queried).isNotNull();
        assertThat(queried.getProductName()).isEqualTo("New Laptop");
        assertThat(callCount).isEqualTo(1);
    }

    /**
     * 测试批量新增订单后批量查询
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>创建 3 个订单：batch-1, batch-2, batch-3</li>
     *   <li>调用 createOrders 方法批量保存到数据库</li>
     *   <li>调用 getOrdersByIds 方法批量查询订单</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>批量创建成功：返回 3 个订单</li>
     *   <li>批量查询成功：返回 3 个订单，callCount=1</li>
     *   <li>数据一致性：查询结果与创建数据一致</li>
     * </ul>
     */
    @Test
    void shouldCreateOrdersInBatchAndQueryFromDatabase() {
        List<OrderEntity> newOrders = new ArrayList<>();
        newOrders.add(OrderEntity.builder().id("batch-1").productName("Batch Laptop 1").quantity(1).price(999.99).build());
        newOrders.add(OrderEntity.builder().id("batch-2").productName("Batch Laptop 2").quantity(2).price(1099.99).build());
        newOrders.add(OrderEntity.builder().id("batch-3").productName("Batch Laptop 3").quantity(3).price(1199.99).build());

        List<OrderEntity> created = orderService.createOrders(newOrders);

        assertThat(created).hasSize(3);

        List<String> ids = new ArrayList<>();
        ids.add("batch-1");
        ids.add("batch-2");
        ids.add("batch-3");

        List<OrderEntity> queried = orderService.getOrdersByIds(ids);
        int callCount = orderService.getCallCount();

        assertThat(queried).hasSize(3);
        assertThat(callCount).isEqualTo(1);
    }

    /**
     * 测试更新订单后缓存失效
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>创建订单 id="update-1"，productName="Original Laptop"</li>
     *   <li>查询订单，触发缓存（callCount=1）</li>
     *   <li>更新订单：productName="Updated Laptop"，quantity=2</li>
     *   <li>再次查询订单（callCount=2，缓存被清除）</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次查询：从数据库获取，callCount=1</li>
     *   <li>更新成功：返回更新后的数据</li>
     *   <li>再次查询：缓存已被 @CacheEvict 清除，重新从数据库获取，callCount=2</li>
     *   <li>数据一致性：查询结果为更新后的数据</li>
     * </ul>
     */
    @Test
    void shouldUpdateOrderAndEvictCache() {
        OrderEntity original = OrderEntity.builder()
                .id("update-1")
                .productName("Original Laptop")
                .quantity(1)
                .price(999.99)
                .build();
        orderService.createOrder(original);

        OrderEntity cached = orderService.getOrderById("update-1");
        int firstCallCount = orderService.getCallCount();

        assertThat(cached).isNotNull();
        assertThat(cached.getProductName()).isEqualTo("Original Laptop");
        assertThat(firstCallCount).isEqualTo(1);

        OrderEntity updated = orderService.updateOrder("update-1", OrderEntity.builder()
                .productName("Updated Laptop")
                .quantity(2)
                .price(899.99)
                .build());

        assertThat(updated.getProductName()).isEqualTo("Updated Laptop");

        OrderEntity afterUpdate = orderService.getOrderById("update-1");
        int secondCallCount = orderService.getCallCount();

        assertThat(afterUpdate.getProductName()).isEqualTo("Updated Laptop");
        assertThat(secondCallCount).isEqualTo(2);
    }

    /**
     * 测试批量更新订单后缓存失效
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>创建 2 个订单：batch-update-1, batch-update-2</li>
     *   <li>批量查询订单，触发缓存（callCount=1）</li>
     *   <li>批量更新订单：修改 productName 和 quantity</li>
     *   <li>清除所有缓存</li>
     *   <li>再次批量查询（callCount=2）</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次批量查询：从数据库获取，callCount=1</li>
     *   <li>批量更新成功：返回 2 个更新后的订单</li>
     *   <li>再次批量查询：缓存已清除，重新从数据库获取，callCount=2</li>
     *   <li>数据一致性：查询结果为更新后的数据</li>
     * </ul>
     */
    @Test
    void shouldUpdateOrdersInBatchAndEvictAllCache() {
        List<OrderEntity> originals = new ArrayList<>();
        originals.add(OrderEntity.builder().id("batch-update-1").productName("Original 1").quantity(1).price(100.0).build());
        originals.add(OrderEntity.builder().id("batch-update-2").productName("Original 2").quantity(2).price(200.0).build());
        orderService.createOrders(originals);

        List<String> ids = new ArrayList<>();
        ids.add("batch-update-1");
        ids.add("batch-update-2");

        List<OrderEntity> cached = orderService.getOrdersByIds(ids);
        int firstCallCount = orderService.getCallCount();

        assertThat(cached).hasSize(2);
        assertThat(firstCallCount).isEqualTo(1);

        List<OrderEntity> updates = new ArrayList<>();
        updates.add(OrderEntity.builder().id("batch-update-1").productName("Updated 1").quantity(10).price(1000.0).build());
        updates.add(OrderEntity.builder().id("batch-update-2").productName("Updated 2").quantity(20).price(2000.0).build());

        List<OrderEntity> updated = orderService.updateOrders(updates);

        assertThat(updated).hasSize(2);

        clearAllCaches();

        List<OrderEntity> afterUpdate = orderService.getOrdersByIds(ids);
        int secondCallCount = orderService.getCallCount();

        assertThat(afterUpdate).hasSize(2);
        assertThat(afterUpdate.get(0).getProductName()).isEqualTo("Updated 1");
        assertThat(afterUpdate.get(1).getProductName()).isEqualTo("Updated 2");
        assertThat(secondCallCount).isEqualTo(2);
    }

    /**
     * 测试删除订单后缓存失效
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>创建订单 id="delete-1"</li>
     *   <li>查询订单，触发缓存（callCount=1）</li>
     *   <li>删除订单</li>
     *   <li>再次查询订单（callCount=2）</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次查询：从数据库获取，callCount=1</li>
     *   <li>删除成功：订单从数据库删除</li>
     *   <li>再次查询：缓存已被 @CacheEvict 清除，查询结果为 null，callCount=2</li>
     * </ul>
     */
    @Test
    void shouldDeleteOrderAndEvictCache() {
        OrderEntity order = OrderEntity.builder()
                .id("delete-1")
                .productName("To Be Deleted")
                .quantity(1)
                .price(100.0)
                .build();
        orderService.createOrder(order);

        OrderEntity cached = orderService.getOrderById("delete-1");
        int firstCallCount = orderService.getCallCount();

        assertThat(cached).isNotNull();
        assertThat(firstCallCount).isEqualTo(1);

        orderService.deleteOrder("delete-1");

        OrderEntity afterDelete = orderService.getOrderById("delete-1");
        int secondCallCount = orderService.getCallCount();

        assertThat(afterDelete).isNull();
        assertThat(secondCallCount).isEqualTo(2);
    }

    /**
     * 测试批量删除订单后缓存失效
     * 
     * <h4>测试步骤</h4>
     * <ol>
     *   <li>创建 2 个订单：batch-delete-1, batch-delete-2</li>
     *   <li>批量查询订单，触发缓存（callCount=1）</li>
     *   <li>批量删除订单</li>
     *   <li>清除所有缓存</li>
     *   <li>再次批量查询（callCount=2）</li>
     * </ol>
     * 
     * <h4>预期结果</h4>
     * <ul>
     *   <li>首次批量查询：从数据库获取 2 个订单，callCount=1</li>
     *   <li>批量删除成功：订单从数据库删除</li>
     *   <li>再次批量查询：查询结果为空列表，callCount=2</li>
     * </ul>
     */
    @Test
    void shouldDeleteOrdersInBatchAndEvictAllCache() {
        List<OrderEntity> orders = new ArrayList<>();
        orders.add(OrderEntity.builder().id("batch-delete-1").productName("Batch Delete 1").quantity(1).price(100.0).build());
        orders.add(OrderEntity.builder().id("batch-delete-2").productName("Batch Delete 2").quantity(2).price(200.0).build());
        orderService.createOrders(orders);

        List<String> ids = new ArrayList<>();
        ids.add("batch-delete-1");
        ids.add("batch-delete-2");

        List<OrderEntity> cached = orderService.getOrdersByIds(ids);
        int firstCallCount = orderService.getCallCount();

        assertThat(cached).hasSize(2);
        assertThat(firstCallCount).isEqualTo(1);

        List<String> deleteIds = new ArrayList<>();
        deleteIds.add("batch-delete-1");
        deleteIds.add("batch-delete-2");
        orderService.deleteOrders(deleteIds);

        clearAllCaches();

        List<OrderEntity> afterDelete = orderService.getOrdersByIds(ids);
        int secondCallCount = orderService.getCallCount();

        assertThat(afterDelete).isEmpty();
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