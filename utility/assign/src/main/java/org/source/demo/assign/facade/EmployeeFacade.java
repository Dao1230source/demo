package org.source.demo.assign.facade;

import org.source.demo.assign.facade.data.EmployeeData;
import org.source.demo.assign.facade.view.OrderView;
import org.source.utility.utils.Streams;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class EmployeeFacade {

    public static final Map<String, EmployeeData> EMPLOYEE_MAP = Map.ofEntries(
            Map.entry("E001", createEmployee("E001", "Alice")),
            Map.entry("E002", createEmployee("E002", "Bob")),
            Map.entry("E003", createEmployee("E003", "Charlie")),
            Map.entry("E004", createEmployee("E004", "David")),
            Map.entry("E005", createEmployee("E005", "Eve"))
    );

    public List<OrderView> getOrderViewList() {
        return List.of(
                createOrderView("E001"),
                createOrderView("E002"),
                createOrderView("E003"),
                createOrderView("E999")
        );
    }

    public List<EmployeeData> findEmployeesByEmpCodes(Collection<String> empCodes) {
        return Streams.map(empCodes, EMPLOYEE_MAP::get).filter(Objects::nonNull).toList();
    }

    public Map<String, EmployeeData> findEmployeeMapByEmpCodes(Collection<String> empCodes) {
        return Streams.toMap(
                Streams.map(empCodes, EMPLOYEE_MAP::get).filter(Objects::nonNull).toList(),
                EmployeeData::getEmpCode
        );
    }

    public List<EmployeeData> findEmployeesThrow(Collection<String> empCodes) {
        throw new UnsupportedOperationException("Test exception");
    }

    /**
     * 根据员工编码单条查询员工信息
     *
     * @param empCode 员工编码
     * @return 员工数据，如果不存在返回null
     */
    public EmployeeData findEmployeeByEmpCode(String empCode) {
        return EMPLOYEE_MAP.get(empCode);
    }

    /**
     * 根据员工编码列表查询员工信息，返回List
     *
     * @param empCodeList 员工编码列表
     * @return 员工数据列表
     */
    public List<EmployeeData> findEmployeesByEmpCodeList(List<String> empCodeList) {
        return Streams.map(empCodeList, EMPLOYEE_MAP::get).filter(Objects::nonNull).toList();
    }

    private static EmployeeData createEmployee(String empCode, String empName) {
        EmployeeData emp = new EmployeeData();
        emp.setEmpCode(empCode);
        emp.setEmpName(empName);
        return emp;
    }

    private static OrderView createOrderView(String empCode) {
        OrderView order = new OrderView();
        order.setEmpCode(empCode);
        return order;
    }
}