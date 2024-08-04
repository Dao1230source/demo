package org.source.demo.spring.log;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.source.demo.spring.facade.param.StudentParam;
import org.source.demo.spring.facade.view.StudentView;
import org.source.demo.spring.log.controller.LogController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class LogTest {
    @Autowired
    private LogController logController;

    /**
     * LogController.select和LogFacade.select都有{@literal @Log}注解，
     * 搜索日志 {@literal save logs}比较连个方法的日志不同
     */
    @Test
    void select() {
        StudentView studentView = logController.select(StudentParam.builder().name("Tom").age(18).build());
        Assertions.assertEquals("Tom", studentView.getName());
    }

    @Test
    void manualSetSomething() {
        StudentView studentView = logController.manualSetSomething(StudentParam.builder().name("Tom").age(18).build());
        Assertions.assertEquals("Tom", studentView.getName());
    }

    @Test
    void manualSetAll() {
        StudentView studentView = logController.manualSetAll(StudentParam.builder().name("Tom").age(18).build());
        Assertions.assertEquals("Tom", studentView.getName());
    }

    @Test
    void selectBatch() {
        List<StudentView> viewList = logController.selectBatch(List.of(StudentParam.builder().name("Tom").age(18).build()));
        Assertions.assertEquals(1, viewList.size());
    }

    @Test
    void selectBatchLogFailed() {
        // 日志处理报错，error code = LOG_METHOD_PARAM_RESULT_MUST_EQUAL_SIZE
        List<StudentView> viewList = logController.selectBatchLogFailed(List.of(StudentParam.builder().name("Tom").age(18).build()));
        Assertions.assertEquals(0, viewList.size());
    }
}
