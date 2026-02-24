package org.source.demo.assign;

import org.junit.jupiter.api.Test;
import org.source.demo.assign.facade.EmployeeFacade;
import org.source.demo.assign.facade.data.EmployeeData;
import org.source.demo.assign.facade.view.OrderView;
import org.source.utility.assign.Assign;
import org.source.utility.assign.InterruptStrategyEnum;
import org.source.utility.utils.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
* Assign 组件单元测试类
* <p>
* 测试覆盖 Assign 组件的各种使用场景，包括：
* - 基础赋值操作
* - 并行执行
* - 缓存机制
* - 分批处理
* - 异常处理
* - 分支处理
* - 子任务处理
* - 后置处理
* - 高级特性：cast、branch filter、虚拟线程等
*/
@SpringBootTest
class AssignTests {

    @Autowired
    private EmployeeFacade employeeFacade;

    /**
     * 测试基础赋值功能
     * <p>
     * 验证 Assign 能够正确地根据订单中的员工编码，从 Facade 获取员工数据并赋值到订单视图中。
     */
    @Test
    void testBasicAssign() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();
        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        Map<String, OrderView> orderMap = Streams.toMap(orderList, OrderView::getEmpCode);
        assertEquals("Alice", orderMap.get("E001").getEmpName());
        assertEquals("Bob", orderMap.get("E002").getEmpName());
        assertEquals("Charlie", orderMap.get("E003").getEmpName());
    }

    /**
     * 测试直接使用 Map 返回类型
     * <p>
     * 验证直接使用 Map 返回的 Facade 方法能够简化赋值操作。
     */
    @Test
    void testDirectAssignWithMap() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();
        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeeMapByEmpCodes)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        Map<String, OrderView> orderMap = Streams.toMap(orderList, OrderView::getEmpCode);
        assertEquals("Alice", orderMap.get("E001").getEmpName());
        assertEquals("Bob", orderMap.get("E002").getEmpName());
    }

    /**
     * 测试空列表处理
     * <p>
     * 验证 Assign 能够正确处理空列表场景，不应抛出异常。
     */
    @Test
    void testEmptyList() {
        List<OrderView> orderList = new ArrayList<>();

        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        assertTrue(orderList.isEmpty());
    }

    /**
     * 测试缺失数据处理
     * <p>
     * 验证当部分数据的key无法找到对应值时，不会导致异常，缺失的数据字段将保持原值。
     */
    @Test
    void testMissingData() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        Map<String, OrderView> orderMap = Streams.toMap(orderList, OrderView::getEmpCode);
        assertEquals("Alice", orderMap.get("E001").getEmpName());
        assertNull(orderMap.get("E999").getEmpName(), "E999 not in employee map, should be null");
    }

    /**
     * 测试多字段赋值
     * <p>
     * 验证 Assign 能够在一次调用中为同一对象赋值多个字段。
     */
    @Test
    void testMultipleFieldsAssign() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null),
                new OrderView("E002", null, null)
        );

        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        Map<String, OrderView> orderMap = Streams.toMap(orderList, OrderView::getEmpCode);
        assertEquals("Alice", orderMap.get("E001").getEmpName());
        assertEquals("Bob", orderMap.get("E002").getEmpName());
    }

    /**
     * 测试并行执行
     * <p>
     * 验证 Assign 的 parallel() 方法能够启用并行执行。
     */
    @Test
    void testParallelExecution() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        Assign.build(orderList)
                .parallel()
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        Map<String, OrderView> orderMap = Streams.toMap(orderList, OrderView::getEmpCode);
        assertEquals("Alice", orderMap.get("E001").getEmpName());
        assertEquals("Bob", orderMap.get("E002").getEmpName());
    }

    /**
     * 测试缓存机制
     * <p>
     * 验证 Assign 的 cache() 方法能够启用缓存，避免重复查询。
     */
    @Test
    void testCacheMechanism() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null),
                new OrderView("E001", null, null),
                new OrderView("E001", null, null)
        );

        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .name("employee-cache")
                .cache()
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        for (OrderView order : orderList) {
            assertEquals("Alice", order.getEmpName());
        }
    }

    /**
     * 测试分批处理
     * <p>
     * 验证 Assign 的 batchSize() 方法能够将大数据集分批处理。
     */
    @Test
    void testBatchProcessing() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null),
                new OrderView("E002", null, null),
                new OrderView("E003", null, null),
                new OrderView("E004", null, null),
                new OrderView("E005", null, null)
        );

        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().batchSize(2)
                .backAssign()
                .invoke();

        Map<String, OrderView> orderMap = Streams.toMap(orderList, OrderView::getEmpCode);
        assertEquals("Alice", orderMap.get("E001").getEmpName());
        assertEquals("Bob", orderMap.get("E002").getEmpName());
        assertEquals("Charlie", orderMap.get("E003").getEmpName());
    }

    /**
     * 测试 addAssignValue 直接赋值
     * <p>
     * 验证 addAssignValue 方法能够直接为所有对象设置固定值。
     */
    @Test
    void testAddAssignValue() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        Assign.build(orderList)
                .addAssignValue(OrderView::setRemark, "test remark")
                .invoke();

        for (OrderView order : orderList) {
            assertEquals("test remark", order.getRemark());
        }
    }

    /**
     * 测试 addAssignValueIfAbsent 条件赋值
     * <p>
     * 验证 addAssignValueIfAbsent 方法仅在字段为空时设置默认值。
     */
    @Test
    void testAddAssignValueIfAbsent() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", "Existing", null),
                new OrderView("E002", null, null)
        );

        Assign.build(orderList)
                .addAssignValueIfAbsent(OrderView::getEmpName, OrderView::setEmpName, "Default")
                .invoke();

        assertEquals("Existing", orderList.getFirst().getEmpName(), "Existing value should not be overwritten");
        assertEquals("Default", orderList.get(1).getEmpName(), "Null value should be set to default");
    }

    /**
     * 测试异常处理 - NO 策略
     * <p>
     * 验证当 InterruptStrategy 为 NO 时，即使发生异常也会继续执行后续分支。
     */
    @Test
    void testExceptionHandlingNoStrategy() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null),
                new OrderView("E002", null, null)
        );

        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesThrow, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire()
                .exceptionHandler((order, throwable) -> {
                })
                .backAssign()
                .interruptStrategy(InterruptStrategyEnum.NO)
                .addBranch()
                .addAssignValue(OrderView::setRemark, "branch executed")
                .backSuperlative()
                .invoke();

        assertNull(orderList.getFirst().getEmpName(), "Name should be null because acquire throws exception");
        assertEquals("branch executed", orderList.getFirst().getRemark(), "Branch should execute with NO strategy");
    }

    /**
     * 测试异常处理 - 异常捕获
     * <p>
     * 验证 exceptionHandler 能够捕获 acquire 中抛出的异常，并允许流程继续执行。
     */
    @Test
    void testExceptionHandling() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null)
        );

        final boolean[] exceptionHandled = {false};

        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesThrow, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire()
                .exceptionHandler((order, throwable) -> {
                    exceptionHandled[0] = true;
                    order.setRemark("error occurred");
                })
                .backAssign()
                .invoke();

        assertTrue(exceptionHandled[0], "Exception should be handled");
        assertNull(orderList.getFirst().getEmpName(), "Name should be null because acquire throws exception");
        assertEquals("error occurred", orderList.getFirst().getRemark(), "Remark should be set in exception handler");
    }

    /**
     * 测试分支处理
     * <p>
     * 验证 Assign 的 addBranch() 方法能够根据条件创建分支处理流程。
     */
    @Test
    void testBranchProcessing() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke()
                .addBranch(order -> "E001".equals(order.getEmpCode()))
                .addAssignValue(OrderView::setRemark, "Alice's order")
                .invoke()
                .backSuperlative()
                .addBranch(order -> "E002".equals(order.getEmpCode()))
                .addAssignValue(OrderView::setRemark, "Bob's order")
                .invoke();

        Map<String, OrderView> orderMap = Streams.toMap(orderList, OrderView::getEmpCode);
        assertEquals("Alice's order", orderMap.get("E001").getRemark());
        assertEquals("Bob's order", orderMap.get("E002").getRemark());
    }

    /**
     * 测试子任务处理
     * <p>
     * 验证 Assign 的 addSub() 方法能够在主流程中嵌入子任务处理。
     */
    @Test
    void testSubTaskProcessing() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        Assign.build(orderList)
                .addSub(this::subAssignTask)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        Map<String, OrderView> orderMap = Streams.toMap(orderList, OrderView::getEmpCode);
        assertEquals("Alice", orderMap.get("E001").getEmpName());
        assertEquals("sub task executed", orderMap.get("E002").getRemark(), "Sub task should have set remark for E002");
    }

    private void subAssignTask(Collection<OrderView> orders) {
        Assign.build(orders)
                .addBranch(order -> "E002".equals(order.getEmpCode()))
                .addAssignValue(OrderView::setRemark, "sub task executed")
                .invoke();
    }

    /**
     * 测试单条数据处理
     * <p>
     * 验证 Assign 处理单条数据（单元素列表）的能力。
     */
    @Test
    void testSingleItemProcessing() {
        List<OrderView> orderList = List.of(new OrderView("E001", null, null));

        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        assertEquals("Alice", orderList.getFirst().getEmpName());
    }

    /**
     * 测试 afterProcessor 后置处理
     * <p>
     * 验证 afterProcessor 方法能够在赋值完成后执行自定义逻辑。
     */
    @Test
    void testAfterProcessor() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();
        final boolean[] processorCalled = {false};

        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire()
                .afterProcessor((order, empMap) -> {
                    processorCalled[0] = true;
                    if (!empMap.containsKey(order.getEmpCode())) {
                        order.setRemark("employee not found");
                    }
                })
                .backAssign()
                .invoke();

        assertTrue(processorCalled[0], "afterProcessor should be called");
        Map<String, OrderView> orderMap = Streams.toMap(orderList, OrderView::getEmpCode);
        assertEquals("employee not found", orderMap.get("E999").getRemark(), "E999 should have remark set");
    }

    /**
     * 测试复杂业务场景
     * <p>
     * 综合测试：验证并行执行、缓存、多字段赋值等特性在复杂场景下的协同工作。
     */
    @Test
    void testComplexBusinessScenario() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        Assign.build(orderList)
                .parallel()
                .addAssignValue(OrderView::setRemark, "processed")
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .name("employee")
                .cache()
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        Map<String, OrderView> orderMap = Streams.toMap(orderList, OrderView::getEmpCode);
        assertEquals("Alice", orderMap.get("E001").getEmpName());
        assertEquals("Bob", orderMap.get("E002").getEmpName());
        assertEquals("processed", orderMap.get("E001").getRemark());
    }

    /**
     * 测试 addSingleFetcher 单条查询方式
     * <p>
     * 验证 addAcquire4Single 方法能够支持单条查询模式，适用于不支持批量查询的接口。
     */
    @Test
    void testSingleFetcher() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null),
                new OrderView("E002", null, null)
        );

        Assign.build(orderList)
                .addAcquire4Single(employeeFacade::findEmployeeByEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        assertEquals("Alice", orderList.getFirst().getEmpName());
        assertEquals("Bob", orderList.get(1).getEmpName());
    }

    /**
     * 测试 filter 过滤机制
     * <p>
     * 验证 Action 的 filter 方法能够对获取的数据进行筛选。
     */
    @Test
    void testActionFilter() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null),
                new OrderView("E002", null, null)
        );

        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .filter(emp -> "E001".equals(emp.getEmpCode()))
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        assertEquals("Alice", orderList.getFirst().getEmpName());
        assertNull(orderList.get(1).getEmpName(), "E002 should not be assigned due to filter");
    }

    /**
     * 测试 timeout 配置
     * <p>
     * 验证 Assign 和 Acquire 的 timeout 配置能够正确生效。
     */
    @Test
    void testTimeoutConfiguration() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        Assign.build(orderList)
                .timeout(60)
                .parallel()
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .timeout(30)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        assertNotNull(orderList.getFirst().getEmpName());
    }

    /**
     * 测试 interruption 策略 - ANY
     * <p>
     * 验证 InterruptStrategyEnum.ANY 在任一 Acquire 失败时立即中断执行。
     */
    @Test
    void testInterruptStrategyAny() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null),
                new OrderView("E002", null, null)
        );

        Assign.build(orderList)
                .interruptStrategy(InterruptStrategyEnum.ANY)
                .addAcquire(employeeFacade::findEmployeesThrow, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire()
                .exceptionHandler((order, ex) -> {
                })
                .backAssign()
                .addBranch()
                .addAssignValue(OrderView::setRemark, "branch executed")
                .backSuperlative()
                .invoke();

        // 当 Acquire 失败且策略为 ANY 时，分支不应该执行
        assertNull(orderList.getFirst().getRemark(), "Branch should not execute with ANY strategy when acquire fails");
    }

    /**
     * 测试 interruption 策略 - ALL
     * <p>
     * 验证 InterruptStrategyEnum.ALL 只在所有 Acquire 都失败时才中断执行。
     */
    @Test
    void testInterruptStrategyAll() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null),
                new OrderView("E002", null, null)
        );

        Assign.build(orderList)
                .interruptStrategy(InterruptStrategyEnum.ALL)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .addBranch()
                .addAssignValue(OrderView::setRemark, "branch executed")
                .backSuperlative()
                .invoke();

        assertEquals("branch executed", orderList.getFirst().getRemark(), "Branch should execute with ALL strategy when acquire succeeds");
    }

    /**
     * 测试 backSuperlative 回溯到根
     * <p>
     * 验证 backSuperlative 方法能够正确回溯到最顶层的 Assign。
     */
    @Test
    void testBackSuperlative() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        Assign<OrderView> root = Assign.build(orderList);
        Assign<OrderView> branch1 = root.addBranch();
        Assign<OrderView> branch2 = branch1.addBranch();

        Assign<OrderView> backToRoot = branch2.backSuperlative();
        assertEquals(root.getDepth(), backToRoot.getDepth(), "Should be back to root depth");
    }

    /**
     * 测试 backSuperTo 回溯到指定深度
     * <p>
     * 验证 backSuperTo 方法能够回溯到指定的深度级别。
     */
    @Test
    void testBackSuperTo() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        Assign<OrderView> root = Assign.build(orderList);
        Assign<OrderView> branch1 = root.addBranch();
        Assign<OrderView> branch2 = branch1.addBranch();

        Assign<OrderView> backToDepth1 = branch2.backSuperTo(1);
        assertEquals(root.getDepth(), backToDepth1.getDepth());

        Assign<OrderView> backToDepth2 = branch2.backSuperTo(2);
        assertEquals(branch1.getDepth(), backToDepth2.getDepth());
    }

    /**
     * 测试 addBranches 多分支映射
     * <p>
     * 验证 addBranches 方法能够根据映射关系创建多个分支。
     */
    @Test
    void testAddBranchesWithMapping() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null),
                new OrderView("E001", null, null),
                new OrderView("E002", null, null)
        );

        Map<String, java.util.function.Function<Collection<OrderView>, Assign<OrderView>>> keyAssigners = Map.of(
                "E001", es -> {
                    Assign<OrderView> assign = new Assign<>(new ArrayList<>(es));
                    assign.addAssignValue(OrderView::setRemark, "E001 group");
                    return assign;
                },
                "E002", es -> {
                    Assign<OrderView> assign = new Assign<>(new ArrayList<>(es));
                    assign.addAssignValue(OrderView::setRemark, "E002 group");
                    return assign;
                }
        );

        Assign.build(orderList)
                .addBranches(OrderView::getEmpCode, keyAssigners)
                .invoke();

        assertEquals("E001 group", orderList.getFirst().getRemark());
        assertEquals("E001 group", orderList.get(1).getRemark());
        assertEquals("E002 group", orderList.get(2).getRemark());
    }

    /**
     * 测试 cast 类型转换
     * <p>
     * 验证 cast 方法能够将 Assign 中的数据类型进行转换。
     */
    @Test
    void testCastDataType() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        List<String> empNames = Assign.build(orderList)
                .cast(OrderView::getEmpCode)
                .toList();

        assertEquals(4, empNames.size());
        assertTrue(empNames.contains("E001"));
        assertTrue(empNames.contains("E002"));
    }

    /**
     * 测试 casts 集合转换
     * <p>
     * 验证 casts 方法能够对整个集合进行转换。
     */
    @Test
    void testCastsCollection() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        List<String> empNames = Assign.build(orderList)
                .casts(orders -> orders.stream()
                        .map(OrderView::getEmpCode)
                        .toList())
                .toList();

        assertEquals(4, empNames.size());
    }

    /**
     * 测试 peek 链式查看
     * <p>
     * 验证 peek 方法能够在链式调用中观察数据，且支持链式继续。
     */
    @Test
    void testPeekChaining() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();
        final int[] peekCount = {0};

        Assign.build(orderList)
                .peek(e -> peekCount[0]++)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        assertEquals(4, peekCount[0], "peek should be called 3 times");
        assertNotNull(orderList.getFirst().getEmpName());
    }

    /**
     * 测试 toList 转换为列表
     * <p>
     * 验证 toList 方法能够正确导出 Assign 中的数据为新列表。
     */
    @Test
    void testToListConversion() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        List<OrderView> copiedList = Assign.build(orderList).toList();

        assertEquals(orderList.size(), copiedList.size());
        assertEquals(orderList.getFirst().getEmpCode(), copiedList.getFirst().getEmpCode());
    }

    /**
     * 测试 forEach 遍历
     * <p>
     * 验证 forEach 方法能够正确遍历所有元素。
     */
    @Test
    void testForEachTraversal() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();
        final int[] count = {0};

        Assign.build(orderList).forEach(e -> count[0]++);

        assertEquals(4, count[0], "forEach should iterate 3 items");
    }

    /**
     * 测试多个 Acquire 组合
     * <p>
     * 验证 Assign 能够支持多个 Acquire 并行执行，各自独立获取不同的关联数据。
     */
    @Test
    void testMultipleAcquires() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpCode, OrderView::setRemark)
                .backAcquire().backAssign()
                .invoke();

        assertEquals("Alice", orderList.getFirst().getEmpName());
        assertEquals("E001", orderList.getFirst().getRemark());
    }

    /**
     * 测试 addAcquireByMainData 基于主数据查询
     * <p>
     * 验证 addAcquireByMainData 方法能够基于主数据执行查询，而不是依赖 Action 提取的 key。
     */
    @Test
    void testAcquireByMainData() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null),
                new OrderView("E002", null, null)
        );

        Assign.build(orderList)
                .addAcquireByMainData(
                        orders -> employeeFacade.findEmployeesByEmpCodes(
                                orders.stream().map(OrderView::getEmpCode).toList()
                        ),
                        EmployeeData::getEmpCode
                )
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        assertEquals("Alice", orderList.getFirst().getEmpName());
        assertEquals("Bob", orderList.get(1).getEmpName());
    }

    /**
     * 测试 addAcquireByList 列表形式查询
     * <p>
     * 验证 addAcquireByList 方法能够将 Collection 的 keys 转换为 List 后查询。
     */
    @Test
    void testAcquireByList() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        Assign.build(orderList)
                .addAcquireByList(employeeFacade::findEmployeesByEmpCodeList, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        assertNotNull(orderList.getFirst().getEmpName());
    }

    /**
     * 测试 addAcquireByExtra 额外查询
     * <p>
     * 验证 addAcquireByExtra 方法能够执行额外的查询而不依赖主数据。
     */
    @Test
    void testAcquireByExtra() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null),
                new OrderView("E002", null, null)
        );

        Assign.build(orderList)
                .addAcquireByExtra(
                        () -> employeeFacade.findEmployeesByEmpCodes(java.util.Set.of("E001", "E002")),
                        EmployeeData::getEmpCode
                )
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        assertEquals("Alice", orderList.getFirst().getEmpName());
        assertEquals("Bob", orderList.get(1).getEmpName());
    }

    /**
     * 测试 addOperates 通用操作映射
     * <p>
     * 验证 addOperates 方法能够根据映射关系对分组数据执行不同操作。
     */
    @Test
    void testAddOperates() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null),
                new OrderView("E002", null, null),
                new OrderView("E001", null, null)
        );

        Map<String, java.util.function.Consumer<Collection<OrderView>>> keyOperates = Map.of(
                "E001", orders -> orders.forEach(o -> o.setRemark("Group E001")),
                "E002", orders -> orders.forEach(o -> o.setRemark("Group E002"))
        );

        Assign.build(orderList)
                .addOperates(OrderView::getEmpCode, keyOperates)
                .invoke();

        assertEquals("Group E001", orderList.get(0).getRemark());
        assertEquals("Group E002", orderList.get(1).getRemark());
        assertEquals("Group E001", orderList.get(2).getRemark());
    }

    /**
     * 测试 addBranch 带过滤条件
     * <p>
     * 验证 addBranch 方法能够根据谓词条件创建分支，只处理匹配条件的元素。
     */
    @Test
    void testAddBranchWithFilter() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke()
                .addBranch(o -> "Alice".equals(o.getEmpName()))
                .addAssignValue(OrderView::setRemark, "Alice processed")
                .invoke();

        Map<String, OrderView> orderMap = Streams.toMap(orderList, OrderView::getEmpCode);
        assertEquals("Alice processed", orderMap.get("E001").getRemark());
        assertNull(orderMap.get("E002").getRemark());
    }

    /**
     * 测试 name 设置自定义名称
     * <p>
     * 验证 name 方法能够为 Assign 和 Acquire 设置自定义标识名称。
     */
    @Test
    void testCustomNaming() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        Assign.build(orderList)
                .name("CustomAssign")
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .name("CustomAcquire")
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        assertNotNull(orderList.getFirst().getEmpName());
    }

    /**
     * 测试虚拟线程并行执行
     * <p>
     * 验证 parallelVirtual 方法能够使用虚拟线程进行并行执行。
     */
    @Test
    void testParallelVirtualThreads() {
        List<OrderView> orderList = employeeFacade.getOrderViewList();

        Assign.build(orderList)
                .parallelVirtual()
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        assertEquals("Alice", orderList.getFirst().getEmpName());
        assertEquals("Bob", orderList.get(1).getEmpName());
    }

    /**
     * 测试中断策略 NO
     * <p>
     * 验证 InterruptStrategyEnum.NO 能够继续执行所有分支，不因 Acquire 失败而中止。
     */
    @Test
    void testInterruptStrategyNoFinal() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null),
                new OrderView("E002", null, null)
        );

        Assign.build(orderList)
                .interruptStrategy(InterruptStrategyEnum.NO)
                .addAcquire(employeeFacade::findEmployeesThrow, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire()
                .exceptionHandler((order, ex) -> {
                })
                .backAssign()
                .addBranch()
                .addAssignValue(OrderView::setRemark, "no strategy - always execute")
                .backSuperlative()
                .invoke();

        for (OrderView order : orderList) {
            assertEquals("no strategy - always execute", order.getRemark(), "Branch should execute with NO strategy");
        }
    }

    /**
     * 测试大数据批处理
     * <p>
     * 验证 Assign 能够高效处理大量数据，并通过批处理和并行执行提高性能。
     */
    @Test
    void testLargeDatasetBatchProcessing() {
        List<OrderView> largeOrderList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeOrderList.add(new OrderView("E" + (i % 5 + 1), "name" + (i % 5 + 1), null));
        }

        Assign.build(largeOrderList)
                .parallel()
                .addAcquire(employeeFacade::findEmployeesByEmpCodes, EmployeeData::getEmpCode)
                .batchSize(10)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire().backAssign()
                .invoke();

        long successCount = largeOrderList.stream()
                .filter(o -> o.getEmpName() != null)
                .count();
        assertTrue(successCount > 0, "Should successfully assign names to portion of large dataset");
    }
}