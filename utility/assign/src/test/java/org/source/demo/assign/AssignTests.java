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
 */
@SpringBootTest
public class AssignTests {

    @Autowired
    private EmployeeFacade employeeFacade;

    /**
     * 测试基础赋值功能
     * <p>
     * 验证 Assign 能够正确地根据订单中的员工编码，从 Facade 获取员工数据并赋值到订单视图中。
     */
    @Test
    public void testBasicAssign() {
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
    public void testDirectAssignWithMap() {
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
    public void testEmptyList() {
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
    public void testMissingData() {
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
    public void testMultipleFieldsAssign() {
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
    public void testParallelExecution() {
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
    public void testCacheMechanism() {
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
    public void testBatchProcessing() {
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
    public void testAddAssignValue() {
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
    public void testAddAssignValueIfAbsent() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", "Existing", null),
                new OrderView("E002", null, null)
        );

        Assign.build(orderList)
                .addAssignValueIfAbsent(OrderView::getEmpName, OrderView::setEmpName, "Default")
                .invoke();

        assertEquals("Existing", orderList.get(0).getEmpName(), "Existing value should not be overwritten");
        assertEquals("Default", orderList.get(1).getEmpName(), "Null value should be set to default");
    }

    /**
     * 测试异常处理 - NO 策略
     * <p>
     * 验证当 InterruptStrategy 为 NO 时，即使发生异常也会继续执行后续分支。
     */
    @Test
    public void testExceptionHandlingNoStrategy() {
        List<OrderView> orderList = List.of(
                new OrderView("E001", null, null),
                new OrderView("E002", null, null)
        );

        Assign.build(orderList)
                .addAcquire(employeeFacade::findEmployeesThrow, EmployeeData::getEmpCode)
                .addAction(OrderView::getEmpCode)
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                .backAcquire()
                .exceptionHandler((order, throwable) -> {})
                .backAssign()
                .interruptStrategy(InterruptStrategyEnum.NO)
                .addBranch()
                .addAssignValue(OrderView::setRemark, "branch executed")
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
    public void testExceptionHandling() {
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
    public void testBranchProcessing() {
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
    public void testSubTaskProcessing() {
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
    public void testSingleItemProcessing() {
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
    public void testAfterProcessor() {
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
    public void testComplexBusinessScenario() {
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
}
