package org.source.spring.log;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.source.spring.log.controller.LogController;
import org.source.spring.log.facade.param.UserParam;
import org.source.spring.log.facade.view.UserView;
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
     * 搜索日志 {@literal save logs}比较两个方法日志的不同
     */
    @Test
    void select() {
        UserView userView = logController.select(UserParam.builder().userId("000001").username("Tom").build());
        Assertions.assertEquals("Tom", userView.getUsername());
    }

    @Test
    void manualSetSomething() {
        UserView userView = logController.manualSetSomething(UserParam.builder().userId("000001").username("Tom").build());
        Assertions.assertEquals("Tom", userView.getUsername());
    }

    @Test
    void manualSetAll() {
        UserView userView = logController.manualSetAll(UserParam.builder().userId("000001").username("Tom").build());
        Assertions.assertEquals("Tom", userView.getUsername());
    }

    @Test
    void selectBatch() {
        List<UserView> viewList = logController.selectBatch(List.of(UserParam.builder().userId("000001").username("Tom").build()));
        Assertions.assertEquals(1, viewList.size());
    }

    @Test
    void selectBatchLogFailed() {
        // 日志处理报错，error code = LOG_METHOD_PARAM_RESULT_MUST_EQUAL_SIZE
        List<UserView> viewList = logController.selectBatchLogFailed(List.of(UserParam.builder().userId("000001").username("Tom").build()));
        Assertions.assertEquals(0, viewList.size());
    }
}
