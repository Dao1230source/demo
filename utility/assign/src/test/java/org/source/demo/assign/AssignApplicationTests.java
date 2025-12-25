package org.source.demo.assign;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.source.demo.assign.facade.EmployeeFacade;
import org.source.demo.assign.facade.data.EmployeeData;
import org.source.demo.assign.facade.view.OrderView;
import org.source.utility.assign.Assign;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
class AssignApplicationTests {
    private EmployeeFacade employeeFacade;

    /**
     * 经典的写法
     */
    @Test
    public void classicCode() {
        // 订单对象中字段：empCode，通常在前端页面展示的时候需要显示用户名称，这里批量给 empName 赋值
        Collection<OrderView> orderList = new ArrayList<>();
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        // 获取empCodes，过滤非空
        Set<String> empCodes = orderList.stream().map(OrderView::getEmpCode).filter(StringUtils::hasText).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(empCodes)) {
            return;
        }
        // 根据empCodes批量查询员工信息
        Collection<EmployeeData> employeeList;
        try {
            employeeList = employeeFacade.findEmployeesByEmpCodes(empCodes);
        } catch (Exception e) {
            // 异常处理
            log.error("查询员工信息异常", e);
            throw new RuntimeException("查询员工信息异常", e);
        }
        // 以empCode为唯一Key转换为Map
        Map<String, EmployeeData> empCodeMap = employeeList.stream()
                .collect(Collectors.toMap(EmployeeData::getEmpCode, Function.identity(), (v1, v2) -> v1));
        // 循环处理orderList，给 empName 赋值
        orderList.forEach(order -> {
            String empCode = order.getEmpCode();
            if (Objects.isNull(empCode)) {
                return;
            }
            EmployeeData employeeDTO = empCodeMap.get(empCode);
            if (Objects.nonNull(employeeDTO)) {
                order.setEmpName(employeeDTO.getEmpName());
            }
        });
    }

    /**
     * Assign
     */
    @Test
    public void assign() {
        // 订单对象中字段：empCode，通常在前端页面展示的时候需要显示用户名称，这里批量给 empName 赋值
        Collection<OrderView> orderList = new ArrayList<>();
        // 创建一个Assign
        Assign.build(orderList)
                // Acquire：获取外部数据
                .addAcquire(ks -> employeeFacade.findEmployeesByEmpCodes(ks), EmployeeData::getEmpCode)
                // Action：指定字段取值
                .addAction(OrderView::getEmpCode)
                // Assemble：赋值
                .addAssemble(EmployeeData::getEmpName, OrderView::setEmpName)
                // 返回Assign并执行
                .backAcquire().backAssign().invoke();
    }

}
