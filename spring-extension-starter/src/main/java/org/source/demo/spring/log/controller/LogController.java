package org.source.demo.spring.log.controller;

import lombok.AllArgsConstructor;
import org.source.demo.spring.facade.param.StudentParam;
import org.source.demo.spring.facade.view.StudentView;
import org.source.demo.spring.log.facade.LogFacade;
import org.source.spring.log.annotation.Log;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * {@literal @RestController}或{@literal @Controller} 自动设置 {@code
 * systemType = LogSystemTypeEnum.WEB
 * }
 */
@AllArgsConstructor
@RequestMapping("/log")
@RestController
public class LogController {
    private final LogFacade facade;

    @RequestMapping("/select")
    @Log(logId = "#param.name", title = "select", desc = "LogController log")
    public StudentView select(StudentParam param) {
        return facade.select(param);
    }

    @RequestMapping("/manualSetSomething")
    public StudentView manualSetSomething(StudentParam param) {
        return facade.manualSetSomething(param);
    }

    @RequestMapping("/manualSetAll")
    public StudentView manualSetAll(StudentParam param) {
        return facade.manualSetAll(param);
    }

    @RequestMapping("/selectBatch")
    public List<StudentView> selectBatch(List<StudentParam> params) {
        return facade.selectBatch(params);
    }

    @RequestMapping("/selectBatchLogFailed")
    public List<StudentView> selectBatchLogFailed(List<StudentParam> params) {
        return facade.selectBatchLogFailed(params);
    }
}
