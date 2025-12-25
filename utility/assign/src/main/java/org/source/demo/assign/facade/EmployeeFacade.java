package org.source.demo.assign.facade;

import org.source.demo.assign.facade.data.EmployeeData;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class EmployeeFacade {

    public List<EmployeeData> findEmployeesByEmpCodes(Collection<String> empCodes) {
        return List.of();
    }

}
